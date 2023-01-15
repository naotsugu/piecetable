package com.mammb.code.syntax;

import java.util.Objects;

public interface Tag {

    int position();
    String name();
    Tag pair();
    void link(Tag that);
    void unlink();
    boolean isLinked();

    static Tag startOf(int position, String name) {
        return new Start(position, name);
    }

    static Tag endOf(int position, String name) {
        return new End(position, name);
    }

    class TagImpl implements Tag {

        private int position;
        private String name;
        private Tag pair;

        public TagImpl(int position, String name) {
            if (position < 0) {
                throw new IllegalArgumentException();
            }
            this.position = position;
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public String name() { return name;}

        @Override
        public int position() { return position; }

        @Override
        public Tag pair() { return pair; }

        @Override
        public void link(Tag that) {
            pair = that;
            if (that.pair() != this) {
                that.link(this);
            }
        }

        @Override
        public void unlink() {
            if (pair != null) {
                Tag tmp = pair;
                pair = null;
                tmp.unlink();
            }
        }

        @Override
        public boolean isLinked() {
            return pair != null;
        }
    }

    class Start extends TagImpl {
        public Start(int position, String name) { super(position, name);}
        @Override
        public void link(Tag that) {
            if (position() >= that.position()) throw new IllegalArgumentException();
            super.link(that);
        }
    }

    class End extends TagImpl {
        public End(int position, String name) { super(position, name);}
        @Override
        public void link(Tag that) {
            if (position() <= that.position()) throw new IllegalArgumentException();
            super.link(that);
        }
    }

}