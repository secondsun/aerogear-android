/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.impl.pipeline;

import org.jboss.aerogear.android.impl.pipeline.paging.WrappingPagedList;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
/**
 *
 * @author summers
 */
@RunWith(RobolectricTestRunner.class)
public class PagedListTest {
    
    @Test
    public void testNext() {
        Pipe pipe = mock(Pipe.class);
        ReadFilter next = new ReadFilter();
        List delegate = new ArrayList();
        ReadFilter previous = new ReadFilter();
        
        next.setLinkUri(URI.create("./next"));
        previous.setLinkUri(URI.create("./previous"));
        
        WrappingPagedList list = new WrappingPagedList(pipe, delegate, next, previous);
        list.next(mock(Callback.class));
        list.previous(mock(Callback.class));
        
        verify(pipe).readWithFilter(eq(next), any(Callback.class));
        verify(pipe).readWithFilter(eq(previous), any(Callback.class));
        
    }
    
}
