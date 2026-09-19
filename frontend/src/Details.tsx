import {useQuery} from '@tanstack/react-query';
import {api} from './api';
import {ErrorBox,Loading} from './components';
import {Dialog} from './components';
import {type Kind,type Entity,type Human,type Car,type Coordinates,display,moods,weapons,carLabel,colors} from './types';
export default function Details({kind,entity:initialEntity,onClose,onEdit}:{kind:Kind;entity:Entity;onClose:()=>void;onEdit:(entity:Entity)=>void}){
 const query=useQuery({queryKey:['entity',kind,initialEntity.id],queryFn:()=>api<Entity>(`/${kind}/${initialEntity.id}`)});
 if(query.isPending)return <Dialog title="Загрузка карточки" onClose={onClose}><Loading/></Dialog>;
 if(query.isError)return <Dialog title="Объект недоступен" onClose={onClose}><ErrorBox error={query.error}/></Dialog>;
 const entity=query.data;
 let rows:[string,unknown][]=[['ID',entity.id],['Версия',entity.version]];
 if(kind==='humans'){
  const h=entity as Human;
  rows.push(['Имя',h.name],['Дата создания',new Date(h.creationDate).toLocaleString('ru-RU')],['Координаты',`#${h.coordinates.id} · (${h.coordinates.x}; ${h.coordinates.y})`],
   ['Настоящий герой',h.realHero?'Да':'Нет'],['Зубочистка',h.hasToothpick?'Есть':'Нет'],['Автомобиль',h.car?carLabel(h.car):'Без автомобиля'],
   ['Крутой автомобиль',h.car?(h.car.cool?'Да':'Нет'):null],['Настроение',h.mood?moods[h.mood]:null],['Скорость удара',h.impactSpeed],
   ['Саундтрек',h.soundtrackName],['Минуты ожидания',h.minutesOfWaiting],['Оружие',weapons[h.weaponType]]);
 }else if(kind==='cars'){const c=entity as Car;rows.push(['Название',c.name],['Цвет',colors[c.color??'']??c.color],['Крутой',c.cool?'Да':'Нет'])}
 else{const c=entity as Coordinates;rows.push(['X',c.x],['Y',c.y])}
 return <Dialog title={`Карточка #${entity.id}`} onClose={onClose}><dl className="details">{rows.map(([key,value])=><div key={key}><dt>{key}</dt><dd>{display(value)}</dd></div>)}</dl><div className="dialog-actions"><button onClick={()=>onEdit(entity)}>Редактировать</button></div></Dialog>;
}
