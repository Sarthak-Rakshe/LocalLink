import { useQuery } from "@tanstack/react-query";
import { Availability } from "../services/api.js";

export function useAvailableDays(serviceId) {
  return useQuery({
    queryKey: ["available-days", serviceId],
    queryFn: async () => Availability.getAvailableDays(serviceId),
    enabled: !!serviceId,
  });
}

export function useAvailableSlots(providerId, serviceId, date) {
  return useQuery({
    queryKey: ["available-slots", providerId, serviceId, date],
    queryFn: async () =>
      Availability.getAvailableSlots(providerId, serviceId, date),
    enabled: !!providerId && !!serviceId && !!date,
    select: (res) => {
      // Normalize to an array of { startTime, endTime, label }
      const arr = Array.isArray(res)
        ? res
        : res?.availableSlots ?? res?.slots ?? [];
      const toLabel = (start, end) => {
        const fmt = (t) => (typeof t === "string" ? t.slice(0, 5) : String(t));
        return start && end ? `${fmt(start)} - ${fmt(end)}` : fmt(start);
      };
      return (arr || []).map((s) => {
        const start = s?.startTime ?? s?.start ?? s;
        const end = s?.endTime ?? s?.end ?? null;
        return { startTime: start, endTime: end, label: toLabel(start, end) };
      });
    },
  });
}
