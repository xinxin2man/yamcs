import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
  standalone: true,
  selector: 'app-event-severity',
  templateUrl: './event-severity.component.html',
  styleUrl: './event-severity.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatTooltip,
  ],
})
export class EventSeverityComponent {

  @Input()
  severity: string;

  @Input()
  grayscale = false;
}
