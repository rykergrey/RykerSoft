import { Menu } from '@base-ui/react/menu';
import { ChevronDown, MonitorSmartphone } from 'lucide-react';
import type { ReactNode } from 'react';
import { type App, type OS, OS_LIST, OS_NAME, countOS } from '../lib/catalog';
import type { Filters } from '../lib/state';
import { PlatformIcon, buttonClass } from './kit';

const POPUP = 'menu-motion min-w-58 p-1 border-2 border-border rounded-md bg-surface shadow-hard outline-none';
const ITEM = 'flex items-center gap-2.5 w-full min-h-10 pointer-coarse:min-h-11 px-2.5 border-l-3 border-transparent font-sans text-sm font-medium text-text cursor-default select-none outline-none data-[highlighted]:bg-raised data-[checked]:border-action data-[checked]:bg-raised [&_svg]:size-4 [&_svg]:text-dim';

function Trigger({ label, value, icon }: { label: string; value: string; icon?: ReactNode }) {
  return (
    <Menu.Trigger className={buttonClass('plain', false, 'data-[popup-open]:border-dim')}>
      {icon}
      <span className="text-faint">{label}</span>
      <span className="text-action">{value}</span>
      <ChevronDown />
    </Menu.Trigger>
  );
}

/** The system filter. "All" is the default; the other three narrow the catalog to one system. */
export function SystemMenu({ apps, value, mine, onChange }: { apps: App[]; value: Filters['os']; mine: OS | null; onChange: (os: Filters['os']) => void }) {
  return (
    <Menu.Root>
      <Trigger label="System" value={value === 'all' ? 'All' : OS_NAME[value]} icon={value === 'all' ? <MonitorSmartphone /> : <PlatformIcon os={value} />} />
      <Menu.Portal>
        <Menu.Positioner align="start" sideOffset={6} className="z-menu">
          <Menu.Popup className={POPUP}>
            <Menu.RadioGroup value={value} onValueChange={(v) => onChange(v as Filters['os'])}>
              <Menu.RadioItem value="all" className={ITEM}>
                <MonitorSmartphone />All systems<span className="ml-auto font-mono text-label text-faint tabular-nums">{apps.length}</span>
              </Menu.RadioItem>
              {OS_LIST.map((os) => (
                <Menu.RadioItem key={os} value={os} className={ITEM}>
                  <PlatformIcon os={os} />{OS_NAME[os]} only<span className="ml-auto font-mono text-label text-faint tabular-nums">{countOS(apps, os)}</span>
                </Menu.RadioItem>
              ))}
            </Menu.RadioGroup>
            <p className="m-0 mt-1 px-2.5 pt-2 pb-1.5 border-t border-border-soft font-mono text-meta text-faint">
              {mine ? `You are on ${OS_NAME[mine]}. Cards offer the ${OS_NAME[mine]} file first.` : 'Your system was not detected.'}
            </p>
          </Menu.Popup>
        </Menu.Positioner>
      </Menu.Portal>
    </Menu.Root>
  );
}

export function SortMenu({ value, onChange }: { value: Filters['sort']; onChange: (s: Filters['sort']) => void }) {
  return (
    <Menu.Root>
      <Trigger label="Sort" value={value === 'name' ? 'Name' : 'Latest release'} />
      <Menu.Portal>
        <Menu.Positioner align="start" sideOffset={6} className="z-menu">
          <Menu.Popup className={POPUP}>
            <Menu.RadioGroup value={value} onValueChange={(v) => onChange(v as Filters['sort'])}>
              <Menu.RadioItem value="date" className={ITEM}>Latest release</Menu.RadioItem>
              <Menu.RadioItem value="name" className={ITEM}>Name, A to Z</Menu.RadioItem>
            </Menu.RadioGroup>
          </Menu.Popup>
        </Menu.Positioner>
      </Menu.Portal>
    </Menu.Root>
  );
}
