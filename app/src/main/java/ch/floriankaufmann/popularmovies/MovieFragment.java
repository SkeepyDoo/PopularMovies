package ch.floriankaufmann.popularmovies;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MovieFragment extends Fragment {

    public ArrayAdapter<String> mMovieAdapter;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String[] data = {
        };
        final List<String> movieList = new ArrayList<String>(Arrays.asList(data));

        mMovieAdapter =
                new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.list_item_movie,
                        R.id.list_item_movie_imageview,
                        movieList);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);

        GridView gridView = (GridView) rootView.findViewById(R.id.listview_movie);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String movie = mMovieAdapter.getItem(position);
                Intent startActivity = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, movie);
                startActivity(startActivity);
            }
        });

        return rootView;
    }

    private void updateMovies() {
        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute("lol");

    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

class FetchMovieTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    public String[] getMovieImageFromJson(String example, int numMovies)
            throws JSONException {
        final String OWM_RESULT = "results";
        final String OWM_PATH = "poster_path";

        JSONObject movieJson = new JSONObject(example);
        JSONArray movieArray = movieJson.getJSONArray(OWM_RESULT);

        String[] resultSting = new String[movieArray.length()];

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movie = movieArray.getJSONObject(i);
            resultSting[i] = movie.getString(OWM_PATH);

        }
        for (String s : resultSting) {
            Log.v(LOG_TAG, "Movies: " + s);
        }
        return resultSting;
    }

    @Override
    protected String[] doInBackground(String... params) {

        if (params.length == 0) return null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String example = null;

        String apiKey = "22d957adb449240fa05ba07d3f187241";
        int numMovies = 10;

        try {
            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/popular?api_key=";
            final String API_KEY = "api_key";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY, apiKey)
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI" + builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            example = buffer.toString();
            Log.v(LOG_TAG, "Forecast JSON String " + example);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        try {
            return getMovieImageFromJson(example, numMovies);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            mMovieAdapter.clear();
            for (String s : result) {
                mMovieAdapter.add(s);
            }
        }
    }
}
}

