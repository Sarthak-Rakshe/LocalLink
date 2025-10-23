import axiosClient from "./axiosClient";
import { API } from "./endpoints";

export const reviewsApi = {
  /**
   * Get average rating (Double) for a given service.
   */
  getAverageForService: async (serviceId) =>
    axiosClient.get(API.REVIEWS.AVERAGE_FOR_SERVICE(serviceId)),

  /**
   * Optionally fetch aggregate for a provider (average, count, etc.).
   */
  getAggregateForProvider: async (providerId) =>
    axiosClient.get(API.REVIEWS.AGGREGATE_FOR_PROVIDER(providerId)),
};

export default reviewsApi;
