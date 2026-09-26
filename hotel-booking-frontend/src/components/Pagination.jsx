export default function Pagination({ current, totalPages, onChange }) {
  if (totalPages <= 1) return null;

  const pages = Array.from({ length: totalPages }, (_, i) => i);

  return (
    <div className="pagination">
      <button
        className="btn btn-ghost btn-small"
        disabled={current === 0}
        onClick={() => onChange(current - 1)}
      >
        ← Назад
      </button>

      <div className="pagination-pages">
        {pages.map((p) => (
          <button
            key={p}
            className={`pagination-page ${p === current ? "active" : ""}`}
            onClick={() => onChange(p)}
          >
            {p + 1}
          </button>
        ))}
      </div>

      <button
        className="btn btn-ghost btn-small"
        disabled={current === totalPages - 1}
        onClick={() => onChange(current + 1)}
      >
        Вперёд →
      </button>
    </div>
  );
}
