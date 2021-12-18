package me.salamander.morebundles.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.lang.annotation.ElementType;
import java.util.Iterator;

public class MBUtil {
    public static ListTag getOrCreateList(CompoundTag tag, String key, int type) {
        if(tag.contains(key, Tag.TAG_LIST)) {
            return tag.getList(key, type);
        }else{
            ListTag list = new ListTag();
            tag.put(key, list);
            return list;
        }
    }
    
    @SafeVarargs
    public static <T> Iterable<T> iterate(Iterable<T>... iterables) {
        return () -> new Iterator<T>() {
            private int index = 0;
            private Iterator<T> iterator = iterables[0].iterator();
            
            @Override
            public boolean hasNext() {
                if(iterator.hasNext()) {
                    return true;
                }
                if(index < iterables.length - 1) {
                    index++;
                    iterator = iterables[index].iterator();
                    return hasNext();
                }
                return false;
            }
            
            @Override
            public T next() {
                return iterator.next();
            }
        };
    }
}
