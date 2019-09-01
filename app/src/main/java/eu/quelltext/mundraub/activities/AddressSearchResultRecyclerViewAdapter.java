package eu.quelltext.mundraub.activities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.AddressSearchResultFragment.SearchResultListener;
import eu.quelltext.mundraub.search.AddressSearchResult;
import eu.quelltext.mundraub.search.IAddressSearch;
import eu.quelltext.mundraub.search.NullAddressSearch;

/**
 * {@link RecyclerView.Adapter} that can display a {@link eu.quelltext.mundraub.search.AddressSearchResult} and makes a call to the
 * specified {@link SearchResultListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AddressSearchResultRecyclerViewAdapter extends RecyclerView.Adapter<AddressSearchResultRecyclerViewAdapter.ViewHolder> implements IAddressSearch.Observer {

    private final SearchResultListener listener;
    private IAddressSearch addressSearch = new NullAddressSearch();

    public AddressSearchResultRecyclerViewAdapter(SearchResultListener listener) {
        this.listener = listener;
        listener.notifyAboutChanges(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_addresssearchresult, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return addressSearch.size();
    }

    @Override
    public void onNewSearchResults(IAddressSearch addressSearch) {
        this.addressSearch = addressSearch;
        notifyDataSetChanged();
        listener.onSearchResult(addressSearch);
    }

    @Override
    public void onSearchError(int errorId) {
        listener.onSearchError(errorId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public AddressSearchResult mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        public void bind(int position) {
            mItem = addressSearch.get(position);
            mContentView.setText(mItem.getDisplayName());

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        listener.onListFragmentInteraction(mItem);
                    }
                }
            });
        }
    }
}
