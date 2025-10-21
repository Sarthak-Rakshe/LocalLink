import { useParams } from "react-router-dom";

export default function ServiceDetails() {
  const { id } = useParams();
  return <div className="text-gray-700">Service details for #{id}</div>;
}
