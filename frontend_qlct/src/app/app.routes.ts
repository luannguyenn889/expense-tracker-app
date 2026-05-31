import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { Dashboard } from './pages/dashboard/dashboard';
import { Transactions } from './pages/transactions/transactions';
import { Categories } from './pages/categories/categories';
import { Wallets } from './pages/wallets/wallets';
import { Budget } from './pages/budget/budget';
import { Reports } from './pages/reports/reports';
import { Settings } from './pages/settings/settings';
import { ListCategory } from './categories/list-category/list-category';
import { AddCategory } from './categories/add-category/add-category';
import { EditCategory } from './categories/edit-category/edit-category';
import { EditUser } from './user/edit-user/edit-user';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: '', component: Dashboard },
  { path: 'transactions', component: Transactions },
  { path: 'categories', component: Categories },
  { path: 'wallets', component: Wallets },
  { path: 'budget', component: Budget },
  { path: 'reports', component: Reports },
  { path: 'settings', component: Settings },
  { path: 'list-category', component: ListCategory },
  { path: 'categories/add', component: AddCategory },
  { path: 'categories/edit/:id', component: EditCategory },
  { path: 'user/edit/:id', component: EditUser },
  { path: '**', redirectTo: '' }
];

