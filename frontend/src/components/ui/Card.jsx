export function Card({ className = "", children }) {
  return <div className={["card", className].join(" ")}>{children}</div>;
}

export function CardBody({ className = "", children }) {
  return <div className={["card-body", className].join(" ")}>{children}</div>;
}
