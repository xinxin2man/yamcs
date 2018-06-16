import { ChangeDetectionStrategy, Component, HostBinding, OnDestroy } from '@angular/core';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { ConnectionInfo, UserInfo } from '@yamcs/client';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { AuthService } from '../../core/services/AuthService';
import { PreferenceStore } from '../../core/services/PreferenceStore';
import { YamcsService } from '../../core/services/YamcsService';
import { SelectInstanceDialog } from '../../shared/dialogs/SelectInstanceDialog';

@Component({
  selector: 'app-root',
  templateUrl: './AppComponent.html',
  styleUrls: ['./AppComponent.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent implements OnDestroy {

  @HostBinding('class')
  componentCssClass: string;

  title = 'Yamcs';

  connectionInfo$: Observable<ConnectionInfo | null>;
  user$: Observable<UserInfo | null>;

  darkMode$: Observable<boolean>;
  showMdbItem$ = new BehaviorSubject<boolean>(false);

  userSubscription: Subscription;

  constructor(
    yamcs: YamcsService,
    private router: Router,
    private authService: AuthService,
    private preferenceStore: PreferenceStore,
    private dialog: MatDialog,
  ) {
    this.connectionInfo$ = yamcs.connectionInfo$;
    this.user$ = authService.userInfo$;

    this.userSubscription = this.user$.subscribe(user => {
      this.showMdbItem$.next(authService.hasSystemPrivilege('GetMissionDatabase'));
    });

    this.darkMode$ = preferenceStore.darkMode$;
    if (preferenceStore.isDarkMode()) {
      this.enableDarkMode();
    }
  }

  openInstanceDialog() {
    this.dialog.open(SelectInstanceDialog, {
      width: '600px',
      autoFocus: false,
    });
  }

  toggleDarkTheme() {
    if (this.preferenceStore.isDarkMode()) {
      this.disableDarkMode();
    } else {
      this.enableDarkMode();
    }
  }

  logout() {
    this.authService.logout(true);
  }

  private enableDarkMode() {
    document.body.classList.add('dark-theme');
    this.preferenceStore.setDarkMode(true);
  }

  private disableDarkMode() {
    document.body.classList.remove('dark-theme');
    this.preferenceStore.setDarkMode(false);
  }

  ngOnDestroy() {
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
    }
  }
}