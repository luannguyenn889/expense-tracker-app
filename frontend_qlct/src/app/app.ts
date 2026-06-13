import { Component } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Sidebar } from './layout/sidebar/sidebar';
import { AppHeader } from './layout/header/header';
import { AppFooter } from './layout/footer/footer';
import { AiChatWidget } from './layout/ai-chat-widget/ai-chat-widget';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, Sidebar, AppHeader, AppFooter, AiChatWidget],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  isLoginPage = false;

  constructor(private router: Router) {
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.isLoginPage = (event as NavigationEnd).urlAfterRedirects === '/login';
      });
  }
}
