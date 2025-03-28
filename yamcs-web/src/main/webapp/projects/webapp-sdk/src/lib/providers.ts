import { FullscreenOverlayContainer, OverlayContainer } from '@angular/cdk/overlay';
import { APP_BASE_HREF } from '@angular/common';
import { EnvironmentProviders, inject, provideAppInitializer, provideExperimentalZonelessChangeDetection, Provider } from '@angular/core';
import { DateAdapter } from '@angular/material/core';
import { MAT_TOOLTIP_DEFAULT_OPTIONS, MAT_TOOLTIP_DEFAULT_OPTIONS_FACTORY, MatTooltipDefaultOptions } from '@angular/material/tooltip';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { UtcDateAdapter } from './components/date-time-input/UtcDateAdapter';
import { AppearanceService } from './services/appearance.service';
import { ConfigService } from './services/config.service';
import { SdkBridge } from './services/sdk-bridge.service';

const matTooltipOptions: MatTooltipDefaultOptions = {
  ...MAT_TOOLTIP_DEFAULT_OPTIONS_FACTORY(),
  disableTooltipInteractivity: true,
};

export function provideUtcNativeDateAdapter(): Provider[] {
  return [
    { provide: DateAdapter, useClass: UtcDateAdapter },
  ];
}

/**
 * base href can be a context path, and is set in index.html
 * so that it can be applied for loading static resources.
 * Here we derive APP_BASE_HREF from it.
 */
export function provideBaseHrefFromIndexHtml(): Provider[] {
  return [
    {
      provide: APP_BASE_HREF,
      useFactory: () => {
        const baseEl = document.getElementsByTagName('base')[0];
        return baseEl.getAttribute('href');
      },
    },
  ];
}

export function provideYamcsMaterialConfiguration(): Provider[] {
  return [
    provideUtcNativeDateAdapter(),
    { provide: MAT_TOOLTIP_DEFAULT_OPTIONS, useValue: matTooltipOptions },
    // The default OverlayContainer does not show overlays if
    // requestFullscreen is used.
    { provide: OverlayContainer, useClass: FullscreenOverlayContainer },
    provideNoopAnimations(),
  ];
}

export function provideConfigInitializer(): EnvironmentProviders[] {
  return [
    provideAppInitializer(() => {
      inject(ConfigService).loadWebsiteConfig();
    }),
  ];
}

// Not intended for use in webcomponents
export function provideSdkBridge(): EnvironmentProviders[] {
  return [
    provideAppInitializer(() => {
      const sdkBridge = inject(SdkBridge);
      sdkBridge.router = inject(Router);
      sdkBridge.appearanceService = inject(AppearanceService);
    }),
  ];
}

export function provideYamcsWebExtension(): (Provider | EnvironmentProviders)[] {
  return [
    provideBaseHrefFromIndexHtml(),
    provideYamcsMaterialConfiguration(),
    provideConfigInitializer(),
    provideExperimentalZonelessChangeDetection(),
  ];
}
