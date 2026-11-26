import type { ReactNode } from "react";

export interface Column<T> {
  key: string;
  header: ReactNode;
  render: (row: T) => ReactNode;
}

/** Generic typed data table — zebra rows, sticky header, hover. */
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
    <div className="tbl-wrap">
      <table className="tbl">
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
