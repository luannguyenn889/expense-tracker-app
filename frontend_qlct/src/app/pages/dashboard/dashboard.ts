import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {

  aiAdvice: string = '';
  isAiThinking: boolean = false;
  username: string = '';

  constructor(private http: HttpClient, private auth: Auth) {}

  ngOnInit() {
    this.auth.currentUser$.subscribe({
      next: (user) => {
        if (user && user.username) {
          this.username = user.username;
        } else {
          this.username = '';
        }
      }
    });
  }

  askAiForAdvice() {
    this.isAiThinking = true; // Bật cờ loading
    this.aiAdvice = '';       // Xóa lời khuyên cũ (nếu có)

    const url = `http://localhost:8080/api/ai/advice?username=${this.username}`;
    this.http.get(url).subscribe({
      next: (res: any) => {
        this.aiAdvice = res.message; // Hứng đoạn text AI trả về
        this.isAiThinking = false;   // Tắt cờ loading
      },
      error: (err) => {
        console.error('Lỗi AI:', err);
        this.aiAdvice = 'Xin lỗi, trợ lý AI hiện đang đi vắng. Hãy thử lại sau nhé.';
        this.isAiThinking = false;
      }
    });
  }
}
