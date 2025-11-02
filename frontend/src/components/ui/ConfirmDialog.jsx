import { useMemo } from "react";
import Modal from "./Modal.jsx";
import Button from "./Button.jsx";

export default function ConfirmDialog({
  open,
  onClose,
  title = "Confirm",
  description = "Are you sure?",
  confirmText = "Confirm",
  cancelText = "Cancel",
  variant = "danger",
  onConfirm,
  loading = false,
}) {
  const footer = useMemo(
    () => (
      <div className="flex items-center justify-end gap-2">
        <Button variant="outline" onClick={onClose} disabled={loading}>
          {cancelText}
        </Button>
        <Button variant={variant} onClick={onConfirm} loading={loading}>
          {confirmText}
        </Button>
      </div>
    ),
    [cancelText, confirmText, loading, onClose, onConfirm, variant]
  );

  return (
    <Modal open={open} onClose={onClose} title={title} footer={footer}>
      <p className="text-sm text-zinc-700">{description}</p>
    </Modal>
  );
}
