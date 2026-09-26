export default function Toast({ message, variant = "default" }) {
  if (!message) return null;
  return <div className={`toast ${variant === "error" ? "error" : ""}`}>{message}</div>;
}
