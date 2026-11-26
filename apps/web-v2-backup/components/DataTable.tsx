import type { ReactNode } from "react";

export interface Column<T> {
  key: string;
  header: ReactNode;
  render: (row: T) => ReactNode;
}

/** Generic, typed data table used by every registry page. */
export default function DataTable<T extends { id: string }>({
  columns,
  rows,
  emptyText = "لا توجد بيانات",
}: {
  columns: Column<T>[];
  rows: T[];
  emptyText?: string;
}) {
  if (!rows.length) return <div className="empty">{emptyText}</div>;
  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((c) => (
              <th key={c.key}>{c.header}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id}>
              {columns.map((c) => (
                <td key={c.key}>{c.render(row)}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
