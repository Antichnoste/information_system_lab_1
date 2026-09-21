const $ = (selector) => document.querySelector(selector);
const humanForm = $('#human-form');
const referenceForm = $('#reference-form');
const titles = { humans: 'Персонаж', cars: 'Автомобиль', coordinates: 'Координаты' };
const columns = {
  humans: [
    'ID',
    'Имя',
    'Координаты',
    'Дата создания',
    'Настоящий герой',
    'Зубочистка',
    'Автомобиль',
    'Настроение',
    'Скорость удара',
    'Саундтрек',
    'Ожидание, мин.',
    'Оружие',
  ],
  cars: ['ID', 'Название', 'Крутой', 'Цвет'],
  coordinates: ['ID', 'X', 'Y'],
};

let user = null;
let section = 'humans';
let page = 0;
let filters = new URLSearchParams(new FormData($('#human-filters')));
let cars = [];
let coordinates = [];
let humanId = null;
let reference = null;
let details = null;
let deletion = null;
let resultPath = null;
let refreshing = false;
let revision = 0;
const pages = { cars: 0, coordinates: 0, results: 0 };
const pageSize = 10;

function escapeHtml(value) {
  const entities = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  };

  return String(value).replace(/[&<>"']/g, (character) => entities[character]);
}

function display(value) {
  if (value === null || value === undefined) return 'Не указано';
  if (typeof value === 'boolean') return value ? 'Да' : 'Нет';
  return String(value);
}

function coordinateLabel(item) {
  return `№ ${item.id}: X = ${item.x}, Y = ${item.y}`;
}

function carLabel(item) {
  return `№ ${item.id}: ${display(item.name)}, цвет: ${display(item.color)}, крутой: ${display(item.cool)}`;
}

function values(kind, item) {
  if (kind === 'cars') return [item.id, item.name, item.cool, item.color];
  if (kind === 'coordinates') return [item.id, item.x, item.y];
  return [
    item.id,
    item.name,
    coordinateLabel(item.coordinates),
    new Date(item.creationDate).toLocaleString('ru-RU'),
    item.realHero,
    item.hasToothpick,
    item.car ? carLabel(item.car) : 'Нет автомобиля',
    item.mood,
    item.impactSpeed,
    item.soundtrackName,
    item.minutesOfWaiting,
    item.weaponType,
  ];
}

function setHtml(element, html) {
  if (element.innerHTML !== html) {
    element.innerHTML = html;
  }
}

function renderTable(target, kind, items) {
  const heading = columns[kind].map((name) => `<th scope="col">${name}</th>`).join('');

  const rows = items
    .map((item) => {
      const cells = values(kind, item)
        .map((value) => `<td>${escapeHtml(display(value))}</td>`)
        .join('');

      return `
      <tr>
        ${cells}
        <td>
          <button data-view="${kind}" data-id="${item.id}">Открыть</button>
          <button data-edit="${kind}" data-id="${item.id}">Изменить</button>
          <button data-delete="${kind}" data-id="${item.id}">Удалить</button>
        </td>
      </tr>
    `;
    })
    .join('');

  const emptyRow = `
    <tr>
      <td colspan="${columns[kind].length + 1}">Объектов нет.</td>
    </tr>
  `;

  setHtml(
    target,
    `
    <table>
      <thead>
        <tr>
          ${heading}
          <th scope="col">Действия</th>
        </tr>
      </thead>
      <tbody>${rows || emptyRow}</tbody>
    </table>
  `,
  );
}

function renderPagedTable(key, kind, items) {
  const count = Math.max(1, Math.ceil(items.length / pageSize));
  pages[key] = Math.min(pages[key], count - 1);
  renderTable(
    $(`#${key}-table`),
    kind,
    items.slice(pages[key] * pageSize, (pages[key] + 1) * pageSize),
  );
  setHtml(
    $(`#${key}-pager`),
    `<button data-page="${key}" data-step="-1" ${pages[key] === 0 ? 'disabled' : ''}>Назад</button>
    Страница ${pages[key] + 1} из ${count}. Всего: ${items.length}.
    <button data-page="${key}" data-step="1" ${pages[key] >= count - 1 ? 'disabled' : ''}>Вперёд</button>`,
  );
}

async function api(path, method = 'GET', body) {
  let response;
  try {
    response = await fetch(`api/${path}`, {
      method,
      credentials: 'same-origin',
      cache: 'no-store',
      headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
      signal: AbortSignal.timeout(15000),
    });
  } catch {
    throw new Error('Не удалось связаться с сервером. Проверьте подключение и повторите попытку.');
  }
  if (!response.ok) {
    if (response.status === 401 && user) showLogin('Сессия завершена. Войдите снова.');
    const error = new Error(`Не удалось выполнить запрос (${response.status}).`);
    error.status = response.status;
    throw error;
  }
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    throw new Error('Сервер вернул неожиданный ответ.');
  }
}

function showError(target, error) {
  target.classList.add('error');
  target.textContent = error.message;
}

async function run(target, work, container) {
  const buttons = container ? [...container.querySelectorAll('button')] : [];
  if (container?.dataset.busy) return;
  target.textContent = '';
  if (target.id === 'message' || target.id === 'operation-message')
    target.classList.remove('error');
  if (container) container.dataset.busy = 'true';
  const disabled = buttons.map((button) => button.disabled);
  buttons.forEach((button) => {
    button.disabled = true;
  });
  try {
    await work();
  } catch (error) {
    showError(target, error);
  } finally {
    buttons.forEach((button, index) => {
      button.disabled = disabled[index];
    });
    if (container) delete container.dataset.busy;
  }
}

function showLogin(message = '') {
  user = null;
  document.body.classList.add('auth-page');
  revision++;
  document.querySelectorAll('dialog[open]').forEach((dialog) => dialog.close());
  $('#app').hidden = true;
  $('#auth').hidden = false;
  $('#auth-error').textContent = message;
  $('#auth-form').elements.password.value = '';
}

function showApp(session) {
  user = session.username;
  document.body.classList.remove('auth-page');
  revision++;
  $('#username').textContent = user;
  $('#auth').hidden = true;
  $('#app').hidden = false;
  $('#auth-form').reset();
  $('#message').textContent = '';
  $('#sync-error').textContent = '';
  resultPath = null;
  $('#operation-results').hidden = true;
  $('#operation-message').textContent = '';
  navigate();
}

function navigate() {
  section = location.hash.slice(1);
  if (!['humans', 'cars', 'coordinates', 'operations'].includes(section)) section = 'humans';
  revision++;
  document.querySelectorAll('main > section').forEach((element) => {
    element.hidden = element.id !== section;
  });
  document.querySelectorAll('nav a').forEach((link) => {
    if (link.hash === `#${section}`) link.setAttribute('aria-current', 'page');
    else link.removeAttribute('aria-current');
  });
  refresh();
}

function fillSelect(select, items, label, empty) {
  const selected = select.value;
  let html =
    `<option value="">${empty}</option>` +
    items.map((item) => `<option value="${item.id}">${escapeHtml(label(item))}</option>`).join('');
  if (selected && !items.some((item) => String(item.id) === selected)) {
    html += `<option value="${escapeHtml(selected)}">№ ${escapeHtml(selected)} — удалён, выберите другой объект</option>`;
  }
  setHtml(select, html);
  select.value = selected;
}

function refreshSelects() {
  fillSelect(humanForm.elements.coordinatesId, coordinates, coordinateLabel, 'Выберите координаты');
  fillSelect(humanForm.elements.carId, cars, carLabel, 'Нет автомобиля');
  if ($('#delete-dialog').open && deletion && deletion.kind !== 'humans') {
    const items = deletion.kind === 'cars' ? cars : coordinates;
    fillSelect(
      $('#delete-form').elements.replacementId,
      items.filter((item) => String(item.id) !== String(deletion.id)),
      deletion.kind === 'cars' ? carLabel : coordinateLabel,
      'Без замены (если нет связей)',
    );
  }
}

async function loadReferences() {
  [cars, coordinates] = await Promise.all([api('cars'), api('coordinates')]);
  refreshSelects();
}

async function refresh() {
  if (!user || refreshing) return;
  refreshing = true;
  const current = revision;
  try {
    const query = new URLSearchParams(filters);
    query.set('page', page);
    const [newCars, newCoordinates, humans, results] = await Promise.all([
      api('cars'),
      api('coordinates'),
      section === 'humans' ? api(`humans?${query}`) : null,
      section === 'operations' && resultPath ? api(resultPath) : null,
    ]);
    if (!user || current !== revision) return;
    cars = newCars;
    coordinates = newCoordinates;
    refreshSelects();
    if (section === 'cars') renderPagedTable('cars', 'cars', cars);
    if (section === 'coordinates') renderPagedTable('coordinates', 'coordinates', coordinates);
    if (humans) {
      const count = Math.max(1, Math.ceil(humans.total / humans.size));
      if (page >= count) {
        page = count - 1;
        revision++;
      } else {
        renderTable($('#humans-table'), 'humans', humans.items);
        $('#human-page').textContent = `Страница ${page + 1} из ${count}. Всего: ${humans.total}.`;
        $('#human-prev').disabled = page === 0;
        $('#human-next').disabled = page >= count - 1;
      }
    }
    if (section === 'operations' && resultPath) {
      $('#operation-results').hidden = false;
      renderPagedTable(
        'results',
        'humans',
        Array.isArray(results) ? results : results ? [results] : [],
      );
    }
    await refreshDetails();
    $('#sync-error').textContent = '';
    $('#updated').textContent = `Обновлено: ${new Date().toLocaleTimeString('ru-RU')}.`;
  } catch (error) {
    if (user) showError($('#sync-error'), error);
  } finally {
    refreshing = false;
    if (user && current !== revision) refresh();
  }
}

function renderDetails(kind, item) {
  const rows = values(kind, item)
    .map(
      (value, index) =>
        `<tr><th scope="row">${columns[kind][index]}</th><td>${escapeHtml(display(value))}</td></tr>`,
    )
    .join('');
  setHtml($('#details-content'), `<table><tbody>${rows}</tbody></table>`);
}

async function refreshDetails() {
  if (!$('#details-dialog').open || !details) return;
  const selected = details;
  try {
    const item = await api(`${selected.kind}/${selected.id}`);
    if (details !== selected || !$('#details-dialog').open) return;
    renderDetails(selected.kind, item);
    $('#details-dialog .error').textContent = '';
    $('#details-edit').disabled = false;
  } catch (error) {
    if (details !== selected || !$('#details-dialog').open) return;
    if (error.status === 404) {
      $('#details-content').textContent = 'Объект удалён другим пользователем.';
      $('#details-edit').disabled = true;
    } else throw error;
  }
}

async function openDetails(kind, id) {
  const item = await api(`${kind}/${id}`);
  details = { kind, id };
  $('#details-title').textContent = `${titles[kind]} № ${id}`;
  $('#details-dialog .error').textContent = '';
  $('#details-edit').disabled = false;
  renderDetails(kind, item);
  $('#details-dialog').showModal();
}

function resetForm(form) {
  form.reset();
  form.querySelector('.error').textContent = '';
  [...form.elements].forEach((input) => input.setCustomValidity?.(''));
}

async function openEditor(kind, id = null) {
  const item = id === null ? null : await api(`${kind}/${id}`);
  if (kind === 'humans') {
    await loadReferences();
    humanId = id;
    resetForm(humanForm);
    refreshSelects();
    if (item) {
      for (const name of [
        'name',
        'realHero',
        'mood',
        'impactSpeed',
        'soundtrackName',
        'minutesOfWaiting',
        'weaponType',
      ]) {
        humanForm.elements[name].value = item[name] === null ? '' : String(item[name]);
      }
      humanForm.elements.hasToothpick.checked = item.hasToothpick;
      humanForm.elements.coordinatesId.value = String(item.coordinates.id);
      humanForm.elements.carId.value = item.car ? String(item.car.id) : '';
    }
    $('#human-title').textContent = id === null ? 'Новый персонаж' : `Изменить персонажа № ${id}`;
    $('#human-dialog').showModal();
  } else {
    reference = { kind, id };
    resetForm(referenceForm);
    $('#car-fields').hidden = kind !== 'cars';
    $('#car-fields').disabled = kind !== 'cars';
    $('#coordinate-fields').hidden = kind !== 'coordinates';
    $('#coordinate-fields').disabled = kind !== 'coordinates';
    if (item && kind === 'cars') {
      referenceForm.elements.name.value = item.name ?? '';
      referenceForm.elements.color.value = item.color ?? '';
      referenceForm.elements.cool.checked = item.cool;
    } else if (item) {
      referenceForm.elements.x.value = item.x;
      referenceForm.elements.y.value = item.y;
    }
    $('#reference-title').textContent =
      `${id === null ? 'Создать' : 'Изменить'}: ${titles[kind]}${id === null ? '' : ` № ${id}`}`;
    $('#reference-dialog').showModal();
  }
}

async function openDelete(kind, id) {
  const item = await api(`${kind}/${id}`);
  if (kind !== 'humans') await loadReferences();
  deletion = { kind, id };
  resetForm($('#delete-form'));
  $('#delete-title').textContent = `Удалить: ${titles[kind]} № ${id}`;
  $('#delete-description').textContent =
    `Удалить ${kind === 'coordinates' ? coordinateLabel(item) : display(item.name)}?`;
  $('#replacement-fields').hidden = kind === 'humans';
  $('#delete-dialog').showModal();
  refreshSelects();
}

function validateNumber(input) {
  input.setCustomValidity('');
  if (input.matches('[data-long]') && input.value !== '') {
    if (!/^[+-]?\d+$/.test(input.value))
      input.setCustomValidity('Введите целое число без дробной части.');
    else if (
      BigInt(input.value) < -9223372036854775808n ||
      BigInt(input.value) > 9223372036854775807n
    ) {
      input.setCustomValidity('Число должно быть от −9223372036854775808 до 9223372036854775807.');
    }
  }
  if (input === referenceForm.elements.y && input.value !== '') {
    const value = Math.fround(Number(input.value));
    if (!Number.isFinite(value) || value <= -629) {
      input.setCustomValidity('Y должен быть конечным числом больше −629 с учётом точности Float.');
    }
  }
}

function validForm(form) {
  form.querySelectorAll('[data-long], [name="y"]').forEach(validateNumber);
  return form.reportValidity();
}

function longValue(input) {
  return input.value === '' ? null : BigInt(input.value).toString();
}

function changed(message) {
  $('#message').classList.remove('error');
  $('#message').textContent = message;
  revision++;
  refresh();
}

$('#auth-form').addEventListener('submit', (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const action = event.submitter?.value || 'login';
  run(
    $('#auth-error'),
    async () => {
      const body = {
        username: form.elements.username.value,
        password: form.elements.password.value,
      };
      if (!body.username.trim() || !body.password.trim())
        throw new Error('Логин и пароль не должны состоять из пробелов.');
      if (action === 'register') {
        await api('auth/register', 'POST', body);
        $('#auth-error').textContent = 'Регистрация выполнена. Выполняется вход…';
      }
      const session = await api('auth/login', 'POST', body);
      location.hash = 'humans';
      showApp(session);
    },
    form,
  );
});

$('#logout').addEventListener('click', () =>
  run($('#sync-error'), async () => {
    await api('auth/logout', 'POST');
    showLogin();
  }),
);

humanForm.addEventListener('submit', (event) => {
  event.preventDefault();
  if (!validForm(humanForm)) return;
  run(
    $('#human-form .error'),
    async () => {
      const fields = humanForm.elements;
      if (!coordinates.some((item) => String(item.id) === fields.coordinatesId.value))
        throw new Error('Выберите существующие координаты.');
      if (fields.carId.value && !cars.some((item) => String(item.id) === fields.carId.value))
        throw new Error('Выберите существующий автомобиль или «Нет автомобиля».');
      const body = {
        name: fields.name.value,
        coordinatesId: fields.coordinatesId.value,
        realHero: fields.realHero.value === 'true',
        hasToothpick: fields.hasToothpick.checked,
        carId: fields.carId.value || null,
        mood: fields.mood.value || null,
        impactSpeed: longValue(fields.impactSpeed),
        soundtrackName: fields.soundtrackName.value,
        minutesOfWaiting: longValue(fields.minutesOfWaiting),
        weaponType: fields.weaponType.value,
      };
      const saved = await api(
        humanId === null ? 'humans' : `humans/${humanId}`,
        humanId === null ? 'POST' : 'PUT',
        body,
      );
      $('#human-dialog').close();
      changed(`Персонаж № ${saved.id} сохранён.`);
    },
    humanForm,
  );
});

referenceForm.addEventListener('submit', (event) => {
  event.preventDefault();
  if (!validForm(referenceForm)) return;
  run(
    $('#reference-form .error'),
    async () => {
      const { kind, id } = reference;
      const fields = referenceForm.elements;
      const body =
        kind === 'cars'
          ? {
              name: fields.name.value || null,
              color: fields.color.value || null,
              cool: fields.cool.checked,
            }
          : { x: longValue(fields.x), y: Number(fields.y.value) };
      const saved = await api(
        id === null ? kind : `${kind}/${id}`,
        id === null ? 'POST' : 'PUT',
        body,
      );
      const items = kind === 'cars' ? cars : coordinates;
      const index = items.findIndex((item) => item.id === saved.id);
      if (index < 0) items.push(saved);
      else items[index] = saved;
      refreshSelects();
      if ($('#human-dialog').open && id === null) {
        humanForm.elements[kind === 'cars' ? 'carId' : 'coordinatesId'].value = String(saved.id);
      }
      $('#reference-dialog').close();
      changed(`${titles[kind]} № ${saved.id}: сохранено.`);
    },
    referenceForm,
  );
});

$('#delete-form').addEventListener('submit', (event) => {
  event.preventDefault();
  run(
    $('#delete-form .error'),
    async () => {
      const { kind, id } = deletion;
      const replacementId = $('#delete-form').elements.replacementId.value;
      const query =
        kind !== 'humans' && replacementId ? `?${new URLSearchParams({ replacementId })}` : '';
      await api(`${kind}/${id}${query}`, 'DELETE');
      $('#delete-dialog').close();
      changed(`${titles[kind]} № ${id}: удалено.`);
    },
    $('#delete-form'),
  );
});

$('#lookup').addEventListener('submit', (event) => {
  event.preventDefault();
  run(
    $('#message'),
    async () => {
      const fields = $('#lookup').elements;
      const id = BigInt(fields.id.value);
      const maximum = fields.kind.value === 'humans' ? 2147483647n : 9223372036854775807n;
      if (id <= 0n || id > maximum) throw new Error(`ID должен быть от 1 до ${maximum}.`);
      await openDetails(fields.kind.value, id.toString());
    },
    $('#lookup'),
  );
});

$('#details-edit').addEventListener('click', () =>
  run($('#details-dialog .error'), async () => {
    await openEditor(details.kind, details.id);
    $('#details-dialog').close();
  }),
);

$('#human-filters').addEventListener('submit', (event) => {
  event.preventDefault();
  filters = new URLSearchParams(new FormData(event.currentTarget));
  page = 0;
  revision++;
  refresh();
});
$('#human-filters').addEventListener('reset', () =>
  setTimeout(() => {
    filters = new URLSearchParams(new FormData($('#human-filters')));
    page = 0;
    revision++;
    refresh();
  }, 0),
);
$('#human-prev').addEventListener('click', () => {
  page = Math.max(0, page - 1);
  revision++;
  refresh();
});
$('#human-next').addEventListener('click', () => {
  page++;
  revision++;
  refresh();
});

function searchResults(path, title) {
  resultPath = path;
  pages.results = 0;
  $('#result-title').textContent = title;
  $('#results-table').textContent = 'Загрузка…';
  $('#results-pager').textContent = '';
  $('#operation-results').hidden = false;
  revision++;
  refresh();
}
$('#minimum').addEventListener('click', () =>
  searchResults('operations/minimum-waiting', 'Минимальное время ожидания'),
);
$('#soundtrack-form').addEventListener('submit', (event) => {
  event.preventDefault();
  const substring = event.currentTarget.elements.substring.value;
  searchResults(
    `operations/soundtrack?${new URLSearchParams({ substring })}`,
    `Саундтрек содержит: «${substring}»`,
  );
});
$('#weapon-form').addEventListener('submit', (event) => {
  event.preventDefault();
  const weaponType = event.currentTarget.elements.weaponType.value;
  if (!confirm(`Удалить одного персонажа с оружием ${weaponType}?`)) return;
  run(
    $('#operation-message'),
    async () => {
      const result = await api(
        `operations/by-weapon?${new URLSearchParams({ weaponType })}`,
        'DELETE',
      );
      $('#operation-message').textContent = result.count
        ? `Удалён персонаж № ${result.id}.`
        : 'Персонажей с таким оружием нет.';
      revision++;
      refresh();
    },
    $('#weapon-form'),
  );
});

document.addEventListener('click', (event) => {
  const button = event.target.closest('button');
  if (!button) return;
  if (button.hasAttribute('data-close')) button.closest('dialog').close();
  if (button.dataset.page) {
    pages[button.dataset.page] = Math.max(
      0,
      pages[button.dataset.page] + Number(button.dataset.step),
    );
    revision++;
    refresh();
  }
  const dialog = button.closest('dialog');
  const target = dialog?.querySelector('.error') || $('#message');
  if (button.dataset.create) run(target, () => openEditor(button.dataset.create));
  if (button.dataset.view) run(target, () => openDetails(button.dataset.view, button.dataset.id));
  if (button.dataset.edit) run(target, () => openEditor(button.dataset.edit, button.dataset.id));
  if (button.dataset.delete)
    run(target, () => openDelete(button.dataset.delete, button.dataset.id));
  if (button.dataset.operation) {
    const operation = button.dataset.operation;
    run(
      $('#operation-message'),
      async () => {
        const result = await api(`operations/${operation}`, 'POST');
        $('#operation-message').textContent = `Изменено персонажей: ${result.count}.`;
        revision++;
        refresh();
      },
      button.closest('fieldset'),
    );
  }
});
document.addEventListener('input', (event) => {
  if (event.target.matches('[data-long], #reference-form [name="y"]')) validateNumber(event.target);
});
document.querySelectorAll('dialog').forEach((dialog) =>
  dialog.addEventListener('cancel', (event) => {
    if (dialog.querySelector('[data-busy]')) event.preventDefault();
  }),
);
window.addEventListener('hashchange', navigate);
setInterval(refresh, 2000);
run($('#auth-error'), async () => {
  try {
    showApp(await api('auth/session'));
  } catch (error) {
    if (error.status !== 401) throw error;
  }
});
