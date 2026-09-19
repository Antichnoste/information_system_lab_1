import {defineConfig} from '@playwright/test';
export default defineConfig({testDir:'./tests',workers:1,timeout:60000,use:{baseURL:process.env.UI_URL??'http://localhost:5173',viewport:{width:1440,height:950},trace:'retain-on-failure'},reporter:'list'});
