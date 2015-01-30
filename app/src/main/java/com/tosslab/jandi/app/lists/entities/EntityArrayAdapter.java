package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.tosslab.jandi.app.lists.FormattedEntity;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 18..
 */
public class EntityArrayAdapter extends ArrayAdapter<FormattedEntity> {
    private List<FormattedEntity> entities;

    public EntityArrayAdapter(Context context, int textViewResourceId, List<FormattedEntity> entities) {
        super(context, textViewResourceId, entities);
        this.entities = entities;
    }

    @Override
    public FormattedEntity getItem(int position) {
        return entities.get(position);
    }

    public int getPosition(int cdpId) {
        for (int i = 0; i < entities.size(); i++) {
            FormattedEntity entity = entities.get(i);
            if (entity.getEntity().id == cdpId) {
                return i;
            }
        }
        return -1;
    }
}
