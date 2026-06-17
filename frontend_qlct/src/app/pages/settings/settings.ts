import { Component } from '@angular/core';
import { EditUser } from '../../user/edit-user/edit-user';

@Component({
  selector: 'app-settings',
  imports: [EditUser],
  template: `
    <div class="settings-container">
      <app-edit-user></app-edit-user>
    </div>
  `,
  styles: [`
    .settings-container {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
    }
  `],
})
export class Settings {}
