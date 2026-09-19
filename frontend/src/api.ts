import { QueryClient } from '@tanstack/react-query';
export class ApiError extends Error {
 constructor(public status:number,message:string,public fields:Record<string,string>={}){super(message)}
}
export const queryClient=new QueryClient({defaultOptions:{queries:{retry:false,staleTime:5000}}});
export async function api<T>(path:string,method='GET',body?:unknown):Promise<T> {
 const response=await fetch('/api'+path,{method,cache:'no-store',credentials:'same-origin',headers:{'Content-Type':'application/json'},body:body===undefined?undefined:JSON.stringify(body)});
 if(!response.ok){
  const error=await response.json().catch(()=>({message:'Сервер вернул некорректный ответ'}));
  if(response.status===401 && path!=='/auth/login') queryClient.setQueryData(['session'],null);
  throw new ApiError(response.status,error.message ?? 'Не удалось выполнить запрос',error.fields ?? {});
 }
 return response.status===204?null as T:response.json();
}
export const refresh=()=>queryClient.invalidateQueries({predicate:q=>q.queryKey[0]!=='session'});
