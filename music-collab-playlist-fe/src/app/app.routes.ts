import { Routes } from '@angular/router';
import {LandingPage} from './landing-page/landing-page';
import {LinkAccount} from './link-account/link-account';

export const routes: Routes = [
  {
    path: '',
    component: LandingPage
  },
  {
    path: 'link-account',
    component: LinkAccount
  },
  // {
  //   path: 'playlist',
  //   component:
  // }
];
