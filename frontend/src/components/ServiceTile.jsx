import { Link } from "react-router-dom";
import { placeholderImageFor } from "./ServiceCard.jsx";

export default function ServiceTile({ service, linkState }) {
  const imageSrc = placeholderImageFor(service);
  return (
    <Link
      to={`/serviceDetails/${service.serviceId}`}
      state={linkState}
      className="snap-start block relative rounded-lg overflow-hidden min-w-[11rem] sm:min-w-[13rem] bg-gray-100 hover:shadow transition-shadow"
    >
      <div className="h-36 w-full overflow-hidden">
        <img
          src={imageSrc}
          alt={service.serviceName}
          className="h-full w-full object-cover"
        />
      </div>
      <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-2">
        <div className="text-white text-sm font-medium truncate">
          {service.serviceName}
        </div>
      </div>
    </Link>
  );
}
