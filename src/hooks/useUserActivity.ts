import { setCookie, getCookie } from "./useCookies";

export const logLastPage = (page: string) => {
  setCookie("lastPage", page);
};

export const getLastPage = () => getCookie("lastPage");

export const logPreference = (key: string, value: string) => {
  setCookie(`pref_${key}`, value);
};

export const getPreference = (key: string) => getCookie(`pref_${key}`);