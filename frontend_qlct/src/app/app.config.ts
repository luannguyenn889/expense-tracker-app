import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { GoogleLoginProvider, SocialAuthServiceConfig, SOCIAL_AUTH_CONFIG } from '@abacritt/angularx-social-login';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(), // Cung cấp HttpClient cho toàn bộ ứng dụng
    {
      provide: 'SocialAuthServiceConfig', // String token for older versions
      useValue: {
        autoLogin: false,
        providers: [
          {
            id: GoogleLoginProvider.PROVIDER_ID,
            provider: new GoogleLoginProvider(
              '569426721711-6fcai7tp032jl9lirj5ov96g7oati346.apps.googleusercontent.com' // Hãy thay bằng Client ID thật của bạn
            )
          }
        ],
        onError: (err) => {
          console.error(err);
        }
      } as SocialAuthServiceConfig,
    },
    {
      provide: SOCIAL_AUTH_CONFIG, // Provide using the exported injection token
      useValue: {
        autoLogin: false,
        providers: [
          {
            id: GoogleLoginProvider.PROVIDER_ID,
            provider: new GoogleLoginProvider(
              '569426721711-6fcai7tp032jl9lirj5ov96g7oati346.apps.googleusercontent.com'
            )
          }
        ],
        onError: (err) => {
          console.error(err);
        }
      } as SocialAuthServiceConfig,
    }
  ]
};
