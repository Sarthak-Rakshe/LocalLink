import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../context/AuthContext.jsx";
import { Availability, Services } from "../../services/api.js";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import Button from "../../components/ui/Button.jsx";
import { Input, Label } from "../../components/ui/Input.jsx";
import Badge from "../../components/ui/Badge.jsx";
import Skeleton from "../../components/ui/Skeleton.jsx";
import toast from "react-hot-toast";
import { Combobox } from "@headlessui/react";

export default function AvailabilityManage() {
  const { user } = useAuth();
  const providerId = user?.id ?? user?.userId;
  const qc = useQueryClient();

  const rulesQ = useQuery({
    queryKey: ["provider-rules", providerId],
    queryFn: async () => Availability.getRulesForProvider(providerId),
    enabled: !!providerId,
  });
  const exceptionsQ = useQuery({
    queryKey: ["provider-exceptions", providerId],
    queryFn: async () => Availability.getExceptionsForProvider(providerId),
    enabled: !!providerId,
  });

  // Provider services for dropdown selection
  const servicesQ = useQuery({
    queryKey: ["provider-services", providerId],
    queryFn: async () =>
      Services.getAll(
        { userId: Number(providerId) },
        {
          page: 0,
          size: 50, // keep light to avoid heavy initial loads
          sortBy: "serviceName",
          sortDir: "asc",
        }
      ),
    enabled: !!providerId,
    staleTime: 300000,
  });

  const [ruleForm, setRuleForm] = useState({
    serviceId: "",
    daysOfWeek: ["MONDAY"],
    startTime: "09:00",
    endTime: "17:00",
  });
  const [excForm, setExcForm] = useState({
    serviceId: "",
    exceptionDate: "",
    newStartTime: "",
    newEndTime: "",
    exceptionType: "CLOSED",
    exceptionReason: "",
  });

  // Assumptions: backend rule/exception DTO includes providerId and fields below.
  const createRule = useMutation({
    mutationFn: async () => {
      const body = {
        serviceProviderId: Number(providerId),
        serviceId: ruleForm.serviceId ? Number(ruleForm.serviceId) : undefined,
        daysOfWeek: Array.isArray(ruleForm.daysOfWeek)
          ? ruleForm.daysOfWeek
          : [ruleForm.daysOfWeek].filter(Boolean),
        startTime: ruleForm.startTime,
        endTime: ruleForm.endTime,
      };
      return Availability.createRule(body);
    },
    onSuccess: () => {
      toast.success("Rule added");
      qc.invalidateQueries({ queryKey: ["provider-rules", providerId] });
    },
    onError: (e) =>
      toast.error(e?.response?.data?.message || "Failed to add rule"),
  });

  const createException = useMutation({
    mutationFn: async () => {
      const body = {
        serviceProviderId: Number(providerId),
        serviceId: excForm.serviceId ? Number(excForm.serviceId) : undefined,
        exceptionDate: excForm.exceptionDate,
        newStartTime: excForm.newStartTime || undefined,
        newEndTime: excForm.newEndTime || undefined,
        exceptionType: excForm.exceptionType,
        exceptionReason: excForm.exceptionReason || undefined,
      };
      return Availability.createException(body);
    },
    onSuccess: () => {
      toast.success("Exception added");
      qc.invalidateQueries({ queryKey: ["provider-exceptions", providerId] });
    },
    onError: (e) =>
      toast.error(e?.response?.data?.message || "Failed to add exception"),
  });

  const deleteRule = useMutation({
    mutationFn: (id) => Availability.deleteRule(id),
    onSuccess: () => {
      toast.success("Rule deleted");
      qc.invalidateQueries({ queryKey: ["provider-rules", providerId] });
    },
    onError: (e) =>
      toast.error(e?.response?.data?.message || "Failed to delete rule"),
  });

  const deleteException = useMutation({
    mutationFn: (id) => Availability.deleteException(id),
    onSuccess: () => {
      toast.success("Exception deleted");
      qc.invalidateQueries({ queryKey: ["provider-exceptions", providerId] });
    },
    onError: (e) =>
      toast.error(e?.response?.data?.message || "Failed to delete exception"),
  });

  const rules = useMemo(() => {
    const raw = rulesQ.data ?? [];
    return Array.isArray(raw?.content)
      ? raw.content
      : Array.isArray(raw)
      ? raw
      : [];
  }, [rulesQ.data]);

  const exceptions = useMemo(() => {
    const raw = exceptionsQ.data ?? [];
    return Array.isArray(raw?.content)
      ? raw.content
      : Array.isArray(raw)
      ? raw
      : [];
  }, [exceptionsQ.data]);

  const services = useMemo(() => {
    const raw = servicesQ.data ?? [];
    return Array.isArray(raw?.content)
      ? raw.content
      : Array.isArray(raw)
      ? raw
      : [];
  }, [servicesQ.data]);

  const getServiceLabel = (sid) => {
    const s = services.find((x) => Number(x?.serviceId) === Number(sid));
    return s
      ? `${s.serviceName} (#${s.serviceId})`
      : sid
      ? `Service #${sid}`
      : "All services";
  };

  function SelectService({ selectedId, onChangeId, label = "Service" }) {
    const [query, setQuery] = useState("");
    const selected =
      services.find((s) => String(s.serviceId) === String(selectedId)) || null;
    const filtered = query
      ? services.filter(
          (s) =>
            String(s.serviceId).includes(query) ||
            s.serviceName?.toLowerCase().includes(query.toLowerCase())
        )
      : services;

    return (
      <div>
        <Label>
          {label} <span className="text-rose-600">*</span>
        </Label>
        <Combobox
          value={selected}
          onChange={(item) => onChangeId(item?.serviceId ?? "")}
        >
          <div className="relative">
            <Combobox.Input
              className="input-base"
              displayValue={(item) =>
                item?.serviceName
                  ? `${item.serviceName} (#${item.serviceId})`
                  : ""
              }
              placeholder="Search services by name or id"
              onChange={(e) => setQuery(e.target.value)}
            />
            <Combobox.Options className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md border bg-white p-1 shadow-lg">
              {filtered.length === 0 && (
                <div className="px-2 py-1 text-sm text-zinc-500">
                  No services found
                </div>
              )}
              {filtered.map((s) => (
                <Combobox.Option
                  key={s.serviceId}
                  value={s}
                  className={({ active }) =>
                    `cursor-pointer rounded-md px-2 py-1 text-sm ${
                      active ? "bg-zinc-100" : ""
                    }`
                  }
                >
                  {s.serviceName}{" "}
                  <span className="text-zinc-500">#{s.serviceId}</span>
                </Combobox.Option>
              ))}
            </Combobox.Options>
          </div>
        </Combobox>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">Manage availability</h1>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <Card title="Rules (recurring)">
          <div className="space-y-3">
            <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
              <SelectService
                selectedId={ruleForm.serviceId}
                onChangeId={(sid) =>
                  setRuleForm((f) => ({ ...f, serviceId: String(sid || "") }))
                }
                label="Service"
              />
              <div className="md:col-span-2">
                <label className="mb-1 block text-sm font-medium">
                  Days of week
                </label>
                <div className="flex flex-wrap gap-2">
                  {"MONDAY TUESDAY WEDNESDAY THURSDAY FRIDAY SATURDAY SUNDAY"
                    .split(" ")
                    .map((d) => {
                      const checked = ruleForm.daysOfWeek.includes(d);
                      return (
                        <label
                          key={d}
                          className="inline-flex items-center gap-2 rounded-md border px-2 py-1 text-sm"
                        >
                          <input
                            type="checkbox"
                            className="size-4"
                            checked={checked}
                            onChange={(e) =>
                              setRuleForm((f) => ({
                                ...f,
                                daysOfWeek: e.target.checked
                                  ? [...f.daysOfWeek, d]
                                  : f.daysOfWeek.filter((x) => x !== d),
                              }))
                            }
                          />
                          {d}
                        </label>
                      );
                    })}
                </div>
              </div>
              <div>
                <Label>Start</Label>
                <Input
                  type="time"
                  value={ruleForm.startTime}
                  onChange={(e) =>
                    setRuleForm((f) => ({ ...f, startTime: e.target.value }))
                  }
                />
              </div>
              <div>
                <Label>End</Label>
                <Input
                  type="time"
                  value={ruleForm.endTime}
                  onChange={(e) =>
                    setRuleForm((f) => ({ ...f, endTime: e.target.value }))
                  }
                />
              </div>
            </div>
            <Button
              onClick={() => {
                const sid = Number(ruleForm.serviceId);
                const hasDays =
                  Array.isArray(ruleForm.daysOfWeek) &&
                  ruleForm.daysOfWeek.length > 0;
                const validTimes =
                  ruleForm.startTime &&
                  ruleForm.endTime &&
                  ruleForm.startTime < ruleForm.endTime;
                if (!sid || sid <= 0)
                  return toast.error("Service ID is required");
                if (!hasDays) return toast.error("Select at least one day");
                if (!validTimes)
                  return toast.error("End time must be after start time");
                createRule.mutate();
              }}
              disabled={createRule.isPending}
            >
              {createRule.isPending ? "Adding…" : "Add rule"}
            </Button>
          </div>

          <div className="mt-4">
            {rulesQ.isLoading && (
              <div className="space-y-2">
                <Skeleton className="h-4 w-28" />
                <Skeleton className="h-6 w-full" />
                <Skeleton className="h-6 w-full" />
              </div>
            )}
            {rulesQ.isError && (
              <div className="text-sm text-red-600">Failed to load rules</div>
            )}
            {!rulesQ.isLoading && !rulesQ.isError && rules.length === 0 && (
              <EmptyState
                title="No rules"
                message="Add weekly recurring availability."
              />
            )}
            {/* Group rules by service */}
            <div className="space-y-3">
              {Object.entries(
                rules.reduce((acc, r) => {
                  const key = r.serviceId ?? "__none";
                  (acc[key] ||= []).push(r);
                  return acc;
                }, {})
              ).map(([sid, items]) => (
                <div key={sid} className="rounded-lg border">
                  <div className="flex items-center justify-between border-b px-3 py-2">
                    <div className="text-sm font-semibold">
                      {getServiceLabel(sid === "__none" ? null : sid)}
                    </div>
                    <Badge className="bg-zinc-100 text-zinc-700">
                      {items.length} rule{items.length !== 1 ? "s" : ""}
                    </Badge>
                  </div>
                  <ul className="divide-y">
                    {items.map((r) => (
                      <li
                        key={r.id ?? r.ruleId}
                        className="flex items-center justify-between px-3 py-2 text-sm"
                      >
                        <div className="text-zinc-700">
                          <span className="font-medium">
                            {Array.isArray(r.daysOfWeek)
                              ? r.daysOfWeek.join(", ")
                              : r.dayOfWeek || r.day || "DAY"}
                          </span>
                          <span className="ml-2 text-zinc-500">
                            {r.startTime} - {r.endTime}
                          </span>
                        </div>
                        <Button
                          variant="outline"
                          onClick={() => deleteRule.mutate(r.id ?? r.ruleId)}
                        >
                          Delete
                        </Button>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          </div>
        </Card>

        <Card title="Exceptions (one-off)">
          <div className="space-y-3">
            <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
              <SelectService
                selectedId={excForm.serviceId}
                onChangeId={(sid) =>
                  setExcForm((f) => ({ ...f, serviceId: String(sid || "") }))
                }
                label="Service"
              />
              <div>
                <Label>
                  Date <span className="text-rose-600">*</span>
                </Label>
                <Input
                  type="date"
                  value={excForm.exceptionDate}
                  onChange={(e) =>
                    setExcForm((f) => ({ ...f, exceptionDate: e.target.value }))
                  }
                  required
                />
              </div>
              <div>
                <Label>Start</Label>
                <Input
                  type="time"
                  value={excForm.newStartTime}
                  onChange={(e) =>
                    setExcForm((f) => ({ ...f, newStartTime: e.target.value }))
                  }
                />
              </div>
              <div>
                <Label>End</Label>
                <Input
                  type="time"
                  value={excForm.newEndTime}
                  onChange={(e) =>
                    setExcForm((f) => ({ ...f, newEndTime: e.target.value }))
                  }
                />
              </div>
              <div>
                <Label>
                  Type <span className="text-rose-600">*</span>
                </Label>
                <select
                  className="w-full rounded-md border px-3 py-2"
                  value={excForm.exceptionType}
                  onChange={(e) =>
                    setExcForm((f) => ({ ...f, exceptionType: e.target.value }))
                  }
                  required
                >
                  <option value="CLOSED">CLOSED</option>
                  <option value="OPEN">OPEN</option>
                </select>
              </div>
              <div className="md:col-span-2">
                <Label>Reason (optional)</Label>
                <Input
                  placeholder="e.g., personal leave, maintenance"
                  value={excForm.exceptionReason}
                  onChange={(e) =>
                    setExcForm((f) => ({
                      ...f,
                      exceptionReason: e.target.value,
                    }))
                  }
                />
              </div>
            </div>
            <Button
              onClick={() => {
                const sid = Number(excForm.serviceId);
                if (!sid || sid <= 0)
                  return toast.error("Service ID is required");
                if (!excForm.exceptionDate)
                  return toast.error("Date is required");
                if (excForm.exceptionType === "OPEN") {
                  if (!excForm.newStartTime || !excForm.newEndTime)
                    return toast.error(
                      "Start and end time are required for OPEN"
                    );
                  if (!(excForm.newStartTime < excForm.newEndTime))
                    return toast.error("End time must be after start time");
                }
                createException.mutate();
              }}
              disabled={createException.isPending}
            >
              {createException.isPending ? "Adding…" : "Add exception"}
            </Button>
          </div>

          <div className="mt-4">
            {exceptionsQ.isLoading && (
              <div className="space-y-2">
                <Skeleton className="h-4 w-36" />
                <Skeleton className="h-6 w-full" />
                <Skeleton className="h-6 w-full" />
              </div>
            )}
            {exceptionsQ.isError && (
              <div className="text-sm text-red-600">
                Failed to load exceptions
              </div>
            )}
            {!exceptionsQ.isLoading &&
              !exceptionsQ.isError &&
              exceptions.length === 0 && (
                <EmptyState
                  title="No exceptions"
                  message="Add one-off closures or openings."
                />
              )}
            <ul className="divide-y">
              {exceptions.map((x) => (
                <li
                  key={x.id ?? x.exceptionId}
                  className="flex items-center justify-between py-2 text-sm"
                >
                  <div className="text-zinc-700">
                    <span className="font-medium">
                      {x.exceptionDate || x.date}
                    </span>
                    {(x.newStartTime || x.startTime) &&
                      (x.newEndTime || x.endTime) && (
                        <span className="ml-2 text-zinc-500">
                          {x.newStartTime || x.startTime} -{" "}
                          {x.newEndTime || x.endTime}
                        </span>
                      )}
                    {(x.exceptionType || x.type) && (
                      <Badge
                        color={
                          (x.exceptionType || x.type) === "CLOSED"
                            ? "red"
                            : "green"
                        }
                        className="ml-2"
                      >
                        {x.exceptionType || x.type}
                      </Badge>
                    )}
                    {x.exceptionReason && (
                      <span className="ml-2 text-zinc-500">
                        — {x.exceptionReason}
                      </span>
                    )}
                  </div>
                  <Button
                    variant="outline"
                    onClick={() =>
                      deleteException.mutate(x.id ?? x.exceptionId)
                    }
                  >
                    Delete
                  </Button>
                </li>
              ))}
            </ul>
          </div>
        </Card>
      </div>
    </div>
  );
}
