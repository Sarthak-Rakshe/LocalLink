export default function Spinner({ size = 16, className }) {
  const px = typeof size === "number" ? `${size}px` : size;
  return (
    <span
      className={className}
      style={{
        display: "inline-block",
        width: px,
        height: px,
        border: "2px solid rgba(0,0,0,0.2)",
        borderTopColor: "currentColor",
        borderRadius: "9999px",
        animation: "spin 0.8s linear infinite",
      }}
      aria-hidden
    />
  );
}
