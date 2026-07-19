/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.phantom;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 *
 * @author jmburu
 */
public class LargeObjectFinalizer extends PhantomReference<Object>{
    public LargeObjectFinalizer(
      Object referent, ReferenceQueue<? super Object> q) {
        super(referent, q);
    }

    public void finalizeResources() {
        // free resources
        System.out.println("clearing ...");
    }
}
