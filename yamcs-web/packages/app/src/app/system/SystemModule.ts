import { NgModule } from '@angular/core';
import { SharedModule } from '../shared/SharedModule';
import { CommandQueuesTable } from './processors/CommandQueuesTable';
import { QueuedCommandsTable } from './processors/QueuedCommandsTable';
import { TmStatsTable } from './processors/TmStatsTable';
import { routingComponents, SystemRoutingModule } from './SystemRoutingModule';
import { RecordComponent } from './table/RecordComponent';
import { ShowEnumDialog } from './table/ShowEnumDialog';
import { SystemPageTemplate } from './template/SystemPageTemplate';
import { SystemToolbar } from './template/SystemToolbar';

const dialogComponents = [
  ShowEnumDialog,
];

@NgModule({
  imports: [
    SharedModule,
    SystemRoutingModule,
  ],
  declarations: [
    routingComponents,
    dialogComponents,
    CommandQueuesTable,
    QueuedCommandsTable,
    RecordComponent,
    SystemPageTemplate,
    SystemToolbar,
    TmStatsTable,
  ],
  entryComponents: [
    dialogComponents,
  ]
})
export class SystemModule {
}
