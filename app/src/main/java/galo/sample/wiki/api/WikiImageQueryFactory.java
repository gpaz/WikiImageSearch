package galo.sample.wiki.api;

import java.util.UUID;

/**
 * Contains a simple factory method that instantiates a {@link WikiImageQuery} object.
 *
 * Created by Galo on 2/26/2015.
 */
public class WikiImageQueryFactory
{
    /**
     * Creates a query with a uniquely identifiable request id along with the input parameters.
     *
     * {@see UUID}
     * @param searchTerm
     * The text to search for.
     * @param maxThumbSize
     * The maximum size an image can be.
     * @param recordCount
     * The max number of records to return.
     * @return
     * A WikiImageQuery that facilitates the http request.
     */
    public static WikiImageQuery generateUniquelyIndentifiableQuery(String searchTerm
            , int maxThumbSize, int recordCount)
    {
        return new WikiImageQuery(searchTerm, UUID.randomUUID().toString(), maxThumbSize, recordCount);
    }
}
