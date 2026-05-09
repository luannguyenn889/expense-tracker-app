import { Component } from '@angular/core';

@Component({
  selector: 'app-reports',
  imports: [],
  template: `
    <div class="page-placeholder">
      <span class="material-symbols-outlined page-placeholder__icon">bar_chart</span>
      <h2>Reports</h2>
      <p>View detailed financial reports and insights.</p>
      <span class="page-placeholder__badge">Coming Soon</span>
    </div>
  `,
  styles: [`
    .page-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh; gap: 12px; color: var(--color-on-surface-variant); text-align: center; padding: 40px 24px; }
    .page-placeholder__icon { font-size: 64px; color: var(--color-primary); }
    .page-placeholder h2 { font-size: 24px; font-weight: 600; color: var(--color-on-surface); }
    .page-placeholder p { font-size: 16px; max-width: 400px; }
    .page-placeholder__badge { margin-top: 8px; padding: 6px 20px; border-radius: 9999px; background-color: var(--color-primary-container); color: var(--color-on-primary-container); font-weight: 600; font-size: 14px; }
  `],
})
export class Reports {}
