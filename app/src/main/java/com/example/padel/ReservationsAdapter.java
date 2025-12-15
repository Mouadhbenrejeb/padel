package com.example.padel;



import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.WriterException;

import java.util.List;

public class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ReservationViewHolder> {

    private List<Reservation> reservationList;

    public ReservationsAdapter(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);
        holder.tvCourt.setText("Court: " + reservation.getCourt());
        holder.tvDate.setText("Date: " + reservation.getDate());
        holder.tvTime.setText("Time: " + reservation.getTime());
        holder.tvPlayers.setText("Players: " + reservation.getPlayers());

        // Generate QR code
        try {
            Bitmap bitmap = generateQrCode(reservation.getQrContent(), 300);
            holder.ivQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // Set click listener to show larger QR code dialog
        holder.itemView.setOnClickListener(v -> showQrCodeDialog(v.getContext(), reservation));
    }

    private void showQrCodeDialog(Context context, Reservation reservation) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_qr_code);

        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvDialogDetails = dialog.findViewById(R.id.tvDialogDetails);
        ImageView ivDialogQrCode = dialog.findViewById(R.id.ivDialogQrCode);
        MaterialButton btnCloseDialog = dialog.findViewById(R.id.btnCloseDialog);

        tvDialogTitle.setText("Reservation QR Code");
        String details = "Court: " + reservation.getCourt() + "\n" +
                "Date: " + reservation.getDate() + "\n" +
                "Time: " + reservation.getTime() + "\n" +
                "Players: " + reservation.getPlayers();
        tvDialogDetails.setText(details);

        // Generate larger QR code for dialog
        try {
            Bitmap bitmap = generateQrCode(reservation.getQrContent(), 600);
            ivDialogQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourt, tvDate, tvTime, tvPlayers;
        ImageView ivQrCode;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourt = itemView.findViewById(R.id.tvItemCourt);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvTime = itemView.findViewById(R.id.tvItemTime);
            tvPlayers = itemView.findViewById(R.id.tvItemPlayers);
            ivQrCode = itemView.findViewById(R.id.ivItemQr);
        }
    }

    private Bitmap generateQrCode(String content, int size) throws WriterException {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }
}
