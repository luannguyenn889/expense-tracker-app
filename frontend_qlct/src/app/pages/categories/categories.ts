import { Component } from '@angular/core';
import {ListCategory} from '../../categories/list-category/list-category';

@Component({
  selector: 'app-categories',
  imports: [
    ListCategory
  ],
  template: `<app-list-category></app-list-category>`
})
export class Categories {}
