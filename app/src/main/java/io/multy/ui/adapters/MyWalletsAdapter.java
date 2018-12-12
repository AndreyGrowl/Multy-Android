/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.util.NativeDataHelper;
import io.realm.RealmResults;
import timber.log.Timber;

public class MyWalletsAdapter extends RecyclerView.Adapter<MyWalletsAdapter.Holder> {

    private List<Wallet> data;
    private CurrenciesRate rates;
    private OnWalletClickListener listener;

    public MyWalletsAdapter(OnWalletClickListener listener, List<Wallet> data) {
        this.listener = listener;
        this.data = data;
        this.rates = RealmManager.getSettingsDao().getCurrenciesRate();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_asset_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Wallet wallet = data.get(position);
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onWalletClick(wallet);
            } else {
                Timber.e("Forgot to set listener?");
            }
        });

        if (!wallet.isValid()) {
            return;
        }

        if (wallet.getCurrencyId() == NativeDataHelper.Blockchain.ETH.getValue() && wallet.isPending()) {
            holder.amount.setText(wallet.getEthWallet().getPendingBalanceLabel());
            holder.amountFiat.setText(wallet.getEthWallet().getFiatPendingBalanceLabel() + wallet.getFiatString());
        } else {
            holder.amount.setText(wallet.getBalanceLabel());
            holder.amountFiat.setText(wallet.getFiatBalanceLabel(rates));
        }

        holder.name.setText(wallet.getWalletName());
        holder.imageChain.setImageResource(wallet.getIconResourceId());
        holder.imageChevron.setVisibility(wallet.getMultisigWallet() != null && wallet.getMultisigWallet().isHavePaymentRequests() || wallet.isSyncing() ? View.INVISIBLE : View.VISIBLE);
        holder.imageWaiting.setVisibility(wallet.isMultisig() && wallet.getMultisigWallet().isHavePaymentRequests() ? View.VISIBLE : View.GONE);
        holder.resync.setVisibility(wallet.isSyncing() ? View.VISIBLE : View.GONE);
        holder.imagePending.setVisibility(wallet.isPending() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return data == null || (data instanceof RealmResults && !((RealmResults) data).isValid()) ? 0 : data.size();
    }

    public void updateRates(CurrenciesRate rates) {
        this.rates = rates;
        notifyDataSetChanged();
    }

    public boolean isValidData() {
        return data == null || !(data instanceof RealmResults) || ((RealmResults) data).isValid();
    }

    public void setData(List<Wallet> data) {
        this.data = data;
        notifyDataSetChanged();
    }

//    public void setAmount(int position, double amount) {
////        data.get(position).setBalance(amount);
//        notifyItemChanged(position);
//    }

    public void setListener(OnWalletClickListener listener) {
        this.listener = listener;
    }

    public List<Wallet> getData() {
        return data;
    }

    public Wallet getItem(int position) {
        return data.get(position);
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name)
        TextView name;
        @BindView(R.id.text_amount)
        TextView amount;
        @BindView(R.id.text_amount_fiat)
        TextView amountFiat;
        @BindView(R.id.text_currency)
        TextView currency;
        @BindView(R.id.image_chain)
        ImageView imageChain;
        @BindView(R.id.image_pending)
        ImageView imagePending;
        @BindView(R.id.text_resync)
        TextView resync;
        @BindView(R.id.image_chevron)
        View imageChevron;
        @BindView(R.id.image_waiting)
        View imageWaiting;


        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
    }
}
