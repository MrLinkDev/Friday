package ru.linkstuff.friday.Executors.Apps;

import android.app.Activity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ru.linkstuff.friday.R;

public class Changelog extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changelog_layout);

        ChangelogAdapter adapter = new ChangelogAdapter(getChangelog());
        RecyclerView recyclerView = findViewById(R.id.changelog_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
    }

    private List<ChangelogItem> getChangelog(){
        List<ChangelogItem> list = new ArrayList<>();
        String line;

        try {
            InputStream inputStream = getAssets().open("ChangeLog.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            while ((line = reader.readLine()) != null){
                String[] cache = line.split(" - ");
                list.add(new ChangelogItem(cache[0], cache[1]));
            }
            reader.close();

        } catch (IOException e){
            e.printStackTrace();
        }

        list.get(list.size() - 1).addCurrentVersionTag();

        return list;
    }

    class ChangelogAdapter extends RecyclerView.Adapter<ChangelogViewHolder>{
        private List<ChangelogItem> items;

        ChangelogAdapter(List<ChangelogItem> items){
            this.items = items;
        }

        @Override
        public ChangelogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.changelog_item, parent, false);
            return new ChangelogViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ChangelogViewHolder holder, int position) {
            final ChangelogItem item = items.get(position);

            holder.version.setText(item.getVersion());
            holder.changes.setText(item.getChanges());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.isSingle()) item.setSingle(false);
                    else item.setSingle(true);

                    holder.changes.setSingleLine(item.isSingle());
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    class ChangelogViewHolder extends RecyclerView.ViewHolder{
        TextView version;
        TextView changes;

        ChangelogViewHolder(View itemView) {
            super(itemView);

            version = itemView.findViewById(R.id.changelog_item_title);
            changes = itemView.findViewById(R.id.changelog_item_changes);
        }
    }

    class ChangelogItem{
        private String version;
        private String changes;
        private boolean isSingle;

        ChangelogItem(String version, String changes){
            this.version = version;
            this.changes = changes;
            isSingle = true;
        }

        String getVersion() {
            return version;
        }

        void addCurrentVersionTag(){
            version += " " + getString(R.string.changelog_current_version);
        }

        String getChanges() {
            return changes;
        }

        boolean isSingle() {
            return isSingle;
        }

        void setSingle(boolean single) {
            isSingle = single;
        }

    }

}

