package com.tosslab.jandi.app.ui.maintab;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.converter.JacksonConverter;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration.CollapseIntegrationBotViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration.IntegrationBotViewHolder;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DemoActivity extends BaseAppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerView = new RecyclerView(DemoActivity.this);
        setContentView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(DemoActivity.this));

        final String service = getIntent().getStringExtra("service");

        Observable.create(new Observable.OnSubscribe<TempObject>() {
            @Override
            public void call(Subscriber<? super TempObject> subscriber) {
                TempObject github = getRestAdapter().getIntegrations(service);
                subscriber.onNext(github);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
            recyclerView.setAdapter(new IntegrationAdapter(o.messages));
        });
    }

    private IntegrationMessage getRestAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint("https://i-bot.jandi.io")
                .setConverter(new JacksonConverter(JacksonMapper.getInstance().getObjectMapper()))
                .build()
                .create(IntegrationMessage.class);
    }

    interface IntegrationMessage {

        @GET("/temp-api/messages/connects/{service}")
        TempObject getIntegrations(@Path("service") String service);
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TempObject {
        public List<ResMessages.TextMessage> messages;
    }

    private static class IntegrationAdapter extends RecyclerView.Adapter<TempViewHolder> {

        private List<ResMessages.TextMessage> textMessages;

        private IntegrationAdapter(List<ResMessages.TextMessage> textMessages) {
            this.textMessages = textMessages;
        }

        @Override
        public TempViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            BodyViewHolder bodyViewHolder = createBodyViewHolder(viewType);
            View view = LayoutInflater.from(JandiApplication.getContext()).inflate(bodyViewHolder.getLayoutId(), parent, false);
            bodyViewHolder.initView(view);
            return new TempViewHolder(view, bodyViewHolder);
        }

        private BodyViewHolder createBodyViewHolder(int viewType) {
            if (viewType == 1) {
                return new IntegrationBotViewHolder();
            } else {
                return new CollapseIntegrationBotViewHolder();
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public void onBindViewHolder(TempViewHolder holder, int position) {
            ResMessages.Link link = new ResMessages.Link();
            link.message = getItem(position);
            holder.bodyViewHolder.bindData(link, 0, 0, 0);
            holder.bodyViewHolder.setLastReadViewVisible(-1, 1);
        }

        private ResMessages.TextMessage getItem(int position) {
            return textMessages.get(position);
        }

        @Override
        public int getItemCount() {
            return textMessages.size();
        }
    }

    private static class TempViewHolder extends RecyclerView.ViewHolder {

        private final BodyViewHolder bodyViewHolder;

        public TempViewHolder(View itemView, BodyViewHolder bodyViewHolder) {
            super(itemView);
            this.bodyViewHolder = bodyViewHolder;
        }
    }
}
