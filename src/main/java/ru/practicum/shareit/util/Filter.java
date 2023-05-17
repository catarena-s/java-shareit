package ru.practicum.shareit.util;

public interface Filter<T extends QFilter> {
    /**
     * Get new filter by based on input
     * @param filter base filter
     * @return new filter
     */
    T getFilter(T filter);
}
