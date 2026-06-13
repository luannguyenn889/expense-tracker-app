import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { TransactionService } from '../../services/transaction-service';

interface Message {
  sender: 'user' | 'ai';
  text: string;
  time: Date;
}

@Component({
  selector: 'app-ai-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-chat-widget.html',
  styleUrl: './ai-chat-widget.css'
})
export class AiChatWidget implements OnInit {
  isOpen: boolean = false;
  chatMessage: string = '';
  isSending: boolean = false;
  messages: Message[] = [];

  // Gợi ý cho người dùng
  suggestions = [
    'Ăn phở 45k',
    'Nhận lương 15tr',
    'Mua cafe 30k',
    'Đổ xăng 50k'
  ];

  constructor(
    private http: HttpClient,
    private transactionService: TransactionService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.messages.push({
      sender: 'ai',
      text: 'Xin chào! Mình là Trợ lý tài chính AI. Bạn có thể nhập nhanh các giao dịch (ví dụ: "ăn phở 35k", "mua sắm quần áo 500k") để mình tự động phân tích và lưu trữ giúp bạn nhé!',
      time: new Date()
    });
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.scrollToBottom();
    }
    this.cdr.detectChanges();
  }

  selectSuggestion(suggestion: string) {
    this.chatMessage = suggestion;
    this.submitChat();
  }

  submitChat() {
    if (!this.chatMessage.trim() || this.isSending) return;

    const userText = this.chatMessage.trim();
    this.messages.push({
      sender: 'user',
      text: userText,
      time: new Date()
    });

    this.chatMessage = '';
    this.isSending = true; // Hiển thị hiệu ứng đang gõ
    this.scrollToBottom(); // Cuộn xuống cuối khung chat
    this.cdr.detectChanges();
    // Tạo payload từ user input
    const payload = { message: userText };
    // Gửi request đến backend
    this.http.post('http://localhost:8080/api/transactions/chat', payload).subscribe({
      next: (res: any) => {
        this.messages.push({
          sender: 'ai',
          text: res.message || 'Đã lưu giao dịch thành công!',
          time: new Date()
        });
        this.isSending = false;
        this.scrollToBottom();
        this.transactionService.notifyTransactionChange();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.messages.push({
          sender: 'ai',
          text: 'Xin lỗi, mình không hiểu câu lệnh này hoặc hệ thống đang gặp lỗi. Bạn hãy thử gõ rõ ràng hơn (ví dụ: "mua cafe 35k").',
          time: new Date()
        });
        this.isSending = false;
        this.scrollToBottom();
        this.cdr.detectChanges();
      }
    });
  }
  
  // Cuộn xuống cuối khung chat
  private scrollToBottom() {
    setTimeout(() => {
      const chatBody = document.querySelector('.chat-widget__body');
      if (chatBody) {
        chatBody.scrollTop = chatBody.scrollHeight;
      }
    }, 100);
  }
}
