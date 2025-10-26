import React, { useEffect, useMemo, useState } from "react";
import Navbar from "../components/layout/Navbar";
import { useAuth } from "../context/AuthContext";
import { serviceService } from "../services/serviceService";
import { availabilityService } from "../services/availabilityService";
import { toast } from "sonner";
import { Button } from "../components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../components/ui/card";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Checkbox } from "../components/ui/checkbox";
import { RadioGroup, RadioGroupItem } from "../components/ui/radio-group";

const DAYS = [
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
  "SUNDAY",
];

const AvailabilityPage = () => {
  const { user, isProvider } = useAuth();
  const [loading, setLoading] = useState(true);
  const [services, setServices] = useState([]);
  const [selectedServiceId, setSelectedServiceId] = useState("");
  const serviceNameById = useMemo(
    () =>
      Object.fromEntries(
        (services || []).map((s) => [s.serviceId, s.serviceName])
      ),
    [services]
  );

  // Rules state
  const [rules, setRules] = useState([]);
  const [ruleDays, setRuleDays] = useState([]);
  const [ruleStart, setRuleStart] = useState("09:00");
  const [ruleEnd, setRuleEnd] = useState("17:00");
  const [creatingRule, setCreatingRule] = useState(false);

  // Exceptions state
  const [exceptions, setExceptions] = useState([]);
  const [exDate, setExDate] = useState("");
  const [exType, setExType] = useState("BLOCKED");
  const [exStart, setExStart] = useState("00:00");
  const [exEnd, setExEnd] = useState("23:59");
  const [exReason, setExReason] = useState("");
  const [creatingEx, setCreatingEx] = useState(false);

  const providerId = user?.userId;

  const canSubmitRule = useMemo(() => {
    return (
      providerId &&
      selectedServiceId &&
      ruleDays.length > 0 &&
      ruleStart &&
      ruleEnd
    );
  }, [providerId, selectedServiceId, ruleDays, ruleStart, ruleEnd]);

  const canSubmitException = useMemo(() => {
    if (!providerId || !selectedServiceId || !exDate) return false;
    // Require times for both types so users can block a partial day or override hours explicitly
    return !!exStart && !!exEnd;
  }, [providerId, selectedServiceId, exDate, exStart, exEnd]);

  useEffect(() => {
    if (!isProvider()) {
      setLoading(false);
      return;
    }
    (async () => {
      try {
        // Load provider services
        const page = await serviceService.getServices(0, 50, "id", "asc", {
          userId: providerId,
        });
        const svc = page?.content || [];
        setServices(svc);
        if (svc.length > 0) setSelectedServiceId(String(svc[0].serviceId));
      } catch (e) {
        console.error(e);
        toast.error("Failed to load your services");
      } finally {
        setLoading(false);
      }
    })();
  }, [isProvider, providerId]);

  useEffect(() => {
    // Load current rules and exceptions for provider
    if (!providerId) return;
    (async () => {
      try {
        const [r, ex] = await Promise.all([
          availabilityService.getRulesByProvider(providerId),
          availabilityService.getExceptionsByProvider(providerId),
        ]);
        setRules(r || []);
        setExceptions(ex || []);
      } catch (e) {
        console.error(e);
        toast.error("Failed to load availability settings");
      }
    })();
  }, [providerId]);

  const toggleDay = (day) => {
    setRuleDays((prev) =>
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day]
    );
  };

  const createRule = async () => {
    if (!canSubmitRule) return;
    setCreatingRule(true);
    try {
      const payload = {
        serviceProviderId: providerId,
        serviceId: Number(selectedServiceId),
        daysOfWeek: ruleDays,
        startTime: ruleStart,
        endTime: ruleEnd,
      };
      const saved = await availabilityService.createRule(payload);
      setRules((prev) => [...prev, saved]);
      toast.success("Availability rule added");
      setRuleDays([]);
    } catch (e) {
      console.error(e);
      toast.error("Failed to create rule");
    } finally {
      setCreatingRule(false);
    }
  };

  const deleteRule = async (ruleId) => {
    try {
      await availabilityService.deleteRule(ruleId);
      setRules((prev) => prev.filter((r) => r.ruleId !== ruleId));
      toast.success("Rule deleted");
    } catch (e) {
      console.error(e);
      toast.error("Failed to delete rule");
    }
  };

  const createException = async () => {
    if (!canSubmitException) return;
    // Basic client-side validation: start must be before end
    if (exStart >= exEnd) {
      toast.error("Start time must be before end time");
      return;
    }
    setCreatingEx(true);
    try {
      const payload = {
        serviceProviderId: providerId,
        serviceId: Number(selectedServiceId),
        exceptionDate: exDate, // YYYY-MM-DD
        exceptionType: exType,
        exceptionReason: exReason || "", // backend expects non-null
        newStartTime: exStart,
        newEndTime: exEnd,
      };
      const saved = await availabilityService.createException(payload);
      setExceptions((prev) => [...prev, saved]);
      toast.success("Exception added");
      // reset but keep sensible defaults for BLOCKED full-day
      setExDate("");
      setExStart("00:00");
      setExEnd("23:59");
      setExReason("");
      setExType("BLOCKED");
    } catch (e) {
      console.error(e);
      toast.error("Failed to create exception");
    } finally {
      setCreatingEx(false);
    }
  };

  const deleteException = async (exceptionId) => {
    try {
      await availabilityService.deleteException(exceptionId);
      setExceptions((prev) =>
        prev.filter((x) => x.exceptionId !== exceptionId)
      );
      toast.success("Exception deleted");
    } catch (e) {
      console.error(e);
      toast.error("Failed to delete exception");
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="mb-2 text-3xl font-bold">Manage Availability</h1>
          <p className="text-muted-foreground">
            Define your working days, hours, and date-specific exceptions
          </p>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          </div>
        ) : !isProvider() ? (
          <Card>
            <CardContent className="py-8 text-center text-sm text-muted-foreground">
              Only providers can manage availability.
            </CardContent>
          </Card>
        ) : services.length === 0 ? (
          <Card>
            <CardContent className="py-8 text-center text-sm text-muted-foreground">
              No services found. Create a service first to set availability.
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-6 md:grid-cols-2">
            <div className="space-y-6">
              <Card>
                <CardHeader>
                  <CardTitle>Service</CardTitle>
                  <CardDescription>
                    Select which service these rules apply to
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-3">
                  <Label htmlFor="service">Service</Label>
                  <select
                    id="service"
                    className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"
                    value={selectedServiceId}
                    onChange={(e) => setSelectedServiceId(e.target.value)}
                  >
                    {services.map((s) => (
                      <option key={s.serviceId} value={s.serviceId}>
                        {s.serviceName}
                      </option>
                    ))}
                  </select>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Add Rule</CardTitle>
                  <CardDescription>
                    Choose days and working hours
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-2 md:grid-cols-3">
                    {DAYS.map((d) => (
                      <label
                        key={d}
                        className="flex cursor-pointer items-center gap-2 text-sm"
                      >
                        <Checkbox
                          checked={ruleDays.includes(d)}
                          onCheckedChange={() => toggleDay(d)}
                          id={`day-${d}`}
                        />
                        <span className="capitalize">{d.toLowerCase()}</span>
                      </label>
                    ))}
                  </div>
                  <div className="grid grid-cols-2 gap-3 md:grid-cols-3">
                    <div className="space-y-1.5">
                      <Label>Start Time</Label>
                      <Input
                        type="time"
                        value={ruleStart}
                        onChange={(e) => setRuleStart(e.target.value)}
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label>End Time</Label>
                      <Input
                        type="time"
                        value={ruleEnd}
                        onChange={(e) => setRuleEnd(e.target.value)}
                      />
                    </div>
                    <div className="flex items-end">
                      <Button
                        className="w-full"
                        disabled={!canSubmitRule || creatingRule}
                        onClick={createRule}
                      >
                        {creatingRule ? "Saving..." : "Add Rule"}
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Add Exception</CardTitle>
                  <CardDescription>
                    Override or block availability for a specific date
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid gap-3 md:grid-cols-2">
                    <div className="space-y-1.5">
                      <Label>Date</Label>
                      <Input
                        type="date"
                        value={exDate}
                        onChange={(e) => setExDate(e.target.value)}
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label>Type</Label>
                      <RadioGroup
                        value={exType}
                        onValueChange={setExType}
                        className="flex gap-4 pt-2"
                      >
                        <div className="flex items-center gap-2">
                          <RadioGroupItem value="BLOCKED" id="blocked" />
                          <Label htmlFor="blocked">Blocked</Label>
                        </div>
                        <div className="flex items-center gap-2">
                          <RadioGroupItem value="OVERRIDE" id="override" />
                          <Label htmlFor="override">Override</Label>
                        </div>
                      </RadioGroup>
                    </div>
                  </div>

                  <div className="grid gap-3 md:grid-cols-2">
                    <div className="space-y-1.5">
                      <Label>
                        {exType === "OVERRIDE" ? "New Start" : "Block From"}
                      </Label>
                      <Input
                        type="time"
                        value={exStart}
                        onChange={(e) => setExStart(e.target.value)}
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label>
                        {exType === "OVERRIDE" ? "New End" : "Block To"}
                      </Label>
                      <Input
                        type="time"
                        value={exEnd}
                        onChange={(e) => setExEnd(e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="space-y-1.5">
                    <Label>Reason (optional)</Label>
                    <Input
                      value={exReason}
                      onChange={(e) => setExReason(e.target.value)}
                      placeholder="e.g. Holiday"
                    />
                  </div>

                  <div>
                    <Button
                      disabled={!canSubmitException || creatingEx}
                      onClick={createException}
                    >
                      {creatingEx ? "Saving..." : "Add Exception"}
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>

            <div className="space-y-6">
              <Card>
                <CardHeader>
                  <CardTitle>Current Rules</CardTitle>
                  <CardDescription>
                    These define recurring availability
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {rules.length === 0 ? (
                    <p className="text-sm text-muted-foreground">
                      No rules yet
                    </p>
                  ) : (
                    <div className="space-y-3">
                      {rules.map((r) => (
                        <div
                          key={r.ruleId}
                          className="flex items-center justify-between rounded-md border p-3 text-sm"
                        >
                          <div>
                            <div className="font-medium">
                              {r.daysOfWeek
                                ?.map((d) => d.toLowerCase())
                                .join(", ")}{" "}
                              · {r.startTime} - {r.endTime}
                            </div>
                            <div className="text-muted-foreground">
                              {serviceNameById[r.serviceId] ||
                                `Service #${r.serviceId}`}
                            </div>
                          </div>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => deleteRule(r.ruleId)}
                          >
                            Delete
                          </Button>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Exceptions</CardTitle>
                  <CardDescription>
                    Date-specific overrides or blocks
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {exceptions.length === 0 ? (
                    <p className="text-sm text-muted-foreground">
                      No exceptions yet
                    </p>
                  ) : (
                    <div className="space-y-3">
                      {exceptions.map((x) => (
                        <div
                          key={x.exceptionId}
                          className="flex items-center justify-between rounded-md border p-3 text-sm"
                        >
                          <div>
                            <div className="font-medium">
                              {x.exceptionDate} · {x.exceptionType}
                            </div>
                            {x.newStartTime && x.newEndTime && (
                              <div className="text-muted-foreground">
                                New: {x.newStartTime} - {x.newEndTime}
                              </div>
                            )}
                            {x.exceptionReason && (
                              <div className="text-muted-foreground">
                                {x.exceptionReason}
                              </div>
                            )}
                            <div className="text-muted-foreground">
                              {serviceNameById[x.serviceId] ||
                                `Service #${x.serviceId}`}
                            </div>
                          </div>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => deleteException(x.exceptionId)}
                          >
                            Delete
                          </Button>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default AvailabilityPage;
