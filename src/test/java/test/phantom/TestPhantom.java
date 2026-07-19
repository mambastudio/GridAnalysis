/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.phantom;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jmburu
 */
public class TestPhantom {
    public static void main(String... args)
    {
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();        
        List<Object> largeObjects = new ArrayList<>();
        List<LargeObjectFinalizer> references = new ArrayList<>();
        
        for (int i = 0; i < 10; ++i) {
            Object largeObject = new Object();
            largeObjects.add(largeObject);
            references.add(new LargeObjectFinalizer(largeObject, referenceQueue));
        }
        
        largeObjects = null;
        System.gc();
        
        Reference<?> referenceFromQueue;
        references.forEach(reference -> {
            System.out.println(reference.isEnqueued());
        });
        
        while ((referenceFromQueue = referenceQueue.poll()) != null) {
            ((LargeObjectFinalizer)referenceFromQueue).finalizeResources();
            referenceFromQueue.clear();
        }
    }
}
