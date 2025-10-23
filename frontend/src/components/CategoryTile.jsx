import { Link } from "react-router-dom";
import { placeholderImageFor } from "./ServiceCard.jsx";

export default function CategoryTile({ category }) {
  const imageSrc = placeholderImageFor({ serviceCategory: category });
  return (
    <Link
      to={`/services?category=${encodeURIComponent(category)}`}
      state={{ from: "home" }}
      className="snap-start block relative rounded-lg overflow-hidden min-w-[11rem] sm:min-w-[13rem] bg-gray-100 hover:shadow transition-shadow"
    >
      <div className="h-36 w-full overflow-hidden">
        <img
          src={imageSrc}
          alt={category}
          className="h-full w-full object-cover"
        />
      </div>
      <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-2">
        <div className="text-white text-sm font-medium truncate">
          {category}
        </div>
      </div>
    </Link>
  );
}
