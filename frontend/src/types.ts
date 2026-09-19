export type Mood = 'SORROW'|'LONGING'|'RAGE'|'FRENZY';
export type Weapon = 'HAMMER'|'PISTOL'|'SHOTGUN'|'RIFLE';
export interface Car { id:number; version:number; name:string|null; cool:boolean; color:string|null }
export interface Coordinates { id:number; version:number; x:string; y:number }
export interface Human {
 id:number; version:number; name:string; coordinates:Coordinates; creationDate:string;
 realHero:boolean; hasToothpick:boolean; car:Car|null; mood:Mood|null;
 impactSpeed:string; soundtrackName:string; minutesOfWaiting:string|null; weaponType:Weapon;
}
export interface Page<T> { items:T[]; total:number; page:number; size:number }
export type Kind = 'humans'|'cars'|'coordinates';
export type Entity = Human|Car|Coordinates;
export interface Session { username:string; csrfToken:string }
export const moods:Record<Mood,string>={SORROW:'Печаль',LONGING:'Тоска',RAGE:'Ярость',FRENZY:'Неистовство'};
export const weapons:Record<Weapon,string>={HAMMER:'Молот',PISTOL:'Пистолет',SHOTGUN:'Дробовик',RIFLE:'Винтовка'};
export const colors:Record<string,string>={RED:'Красный',BLUE:'Синий',GREEN:'Зелёный',BLACK:'Чёрный',WHITE:'Белый',YELLOW:'Жёлтый'};
export const display = (value:unknown) => value===null || value===undefined ? '—' : String(value)==='' ? '«пустая строка»' : String(value);
export const carLabel = (car:Car) => `${car.name ?? 'Без названия'} · ${colors[car.color??''] ?? car.color ?? 'цвет не указан'} · #${car.id}`;
