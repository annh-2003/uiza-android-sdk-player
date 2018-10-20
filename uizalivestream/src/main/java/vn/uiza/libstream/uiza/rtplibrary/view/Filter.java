package vn.uiza.libstream.uiza.rtplibrary.view;

import vn.uiza.libstream.uiza.encoder.input.gl.render.filters.BaseFilterRender;

public class Filter {

    private int position;
    private BaseFilterRender baseFilterRender;

    public Filter() {
    }

    public Filter(int position, BaseFilterRender baseFilterRender) {
        this.position = position;
        this.baseFilterRender = baseFilterRender;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public BaseFilterRender getBaseFilterRender() {
        return baseFilterRender;
    }

    public void setBaseFilterRender(BaseFilterRender baseFilterRender) {
        this.baseFilterRender = baseFilterRender;
    }
}
