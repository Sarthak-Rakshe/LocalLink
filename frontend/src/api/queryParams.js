// Helpers to build consistent query params per service conventions
// Some services use kebab-case (sort-by, sort-dir) while others use camelCase (sortBy, sortDir).

export const usersPagingParams = ({
  page = 0,
  size = 10,
  sortBy = "id",
  sortDir = "asc",
} = {}) => ({
  "sort-by": sortBy,
  "sort-dir": sortDir,
  page,
  size,
});

export const bookingsPagingParams = ({
  page = 0,
  size = 10,
  sortBy = "id",
  sortDir = "asc",
} = {}) => ({
  "sort-by": sortBy,
  "sort-dir": sortDir,
  page,
  size,
});

export const servicesPagingParams = ({
  page = 0,
  size = 10,
  sortBy = "id",
  sortDir = "asc",
} = {}) => ({
  page,
  size,
  sortBy,
  sortDir,
});

export const reviewsPagingParams = ({
  page = 0,
  size = 10,
  sortBy = "id",
  sortDir = "asc",
} = {}) => ({
  page,
  size,
  sortBy,
  sortDir,
});

export const paymentsPagingParams = ({
  page = 0,
  size = 10,
  sortBy = "id",
  sortDir = "asc",
} = {}) => ({
  page,
  size,
  sortBy,
  sortDir,
});
