package mad9132.planets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mad9132.planets.model.PlanetPOJO;

/**
 * PlanetAdapter.
 *
 */
public class PlanetAdapter extends RecyclerView.Adapter<PlanetAdapter.ViewHolder> {

    private Map<Integer, Bitmap> mBitmaps;
    private Context              mContext;
    private List<PlanetPOJO>     mPlanets;

    public PlanetAdapter(Context context, List<PlanetPOJO> planets) {
        this.mContext = context;
        this.mPlanets = planets;
        this.mBitmaps = new HashMap<>();
    }

    @Override
    public PlanetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View planetView = inflater.inflate(R.layout.list_planet, parent, false);
        ViewHolder viewHolder = new ViewHolder(planetView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PlanetAdapter.ViewHolder holder, int position) {
        final PlanetPOJO aPlanet = mPlanets.get(position);

        holder.tvName.setText(aPlanet.getName());

        // Lazy load the images
        // IF the image's bitmap is in memory, get it and display it
        // ELSE fetch the image as a bitmap in a background task
        if (mBitmaps.containsKey(aPlanet.getPlanetId())) {
            Bitmap bitmap = mBitmaps.get(aPlanet.getPlanetId());
            holder.imageView.setImageBitmap(bitmap);
        } else {
            ImageDownloadTask task = new ImageDownloadTask();
            task.setViewHolder(holder);
            task.execute(aPlanet);
        }

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(mContext, "You long clicked " + aPlanet.getName(),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlanets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvName;
        public ImageView imageView;
        public View mView;

        public ViewHolder(View planetView) {
            super(planetView);

            tvName = (TextView) planetView.findViewById(R.id.planetNameText);
            imageView = (ImageView) planetView.findViewById(R.id.imageView);
            mView = planetView;
        }
    }

    private class ImageDownloadTask extends AsyncTask<PlanetPOJO, Void, Bitmap> {
        private static final String PHOTOS_BASE_URL = "https://planets.mybluemix.net/planets/";
        private PlanetPOJO mPlanet;
        private ViewHolder mHolder;

        public void setViewHolder(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        protected Bitmap doInBackground(PlanetPOJO... planets) {

            mPlanet = planets[0];
            String imageUrl = PHOTOS_BASE_URL + mPlanet.getPlanetId() + "/image";
            InputStream in = null;

            try {
                in = (InputStream) new URL(imageUrl).getContent();
                return BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if ( bitmap != null ) {
                mHolder.imageView.setImageBitmap(bitmap);
                mBitmaps.put(mPlanet.getPlanetId(), bitmap);
            }
        }
    }
}
