package com.androiddevs.mvvmnewsapp.util

import android.graphics.drawable.PictureDrawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.centerCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class ImageUtils {
    companion object{
        fun setImage(root: View, imageUrl: String, imageView: ImageView){
            if(!imageUrl.isNullOrEmpty()){
                val pattern = Regex("^.svg")
                if(pattern.containsMatchIn(imageUrl))
                    setToSVGImageView(root,imageUrl,imageView)
                else
                    setToImageView(root, imageUrl, imageView)
            }
        }
        fun setToSVGImageView(root: View, imageUrl: String, imageView: ImageView){
            GlideApp.with(root)
                    .`as`(PictureDrawable::class.java)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(MyAppGlideModule.SvgSoftwareLayerSetter())
                    .load(imageUrl)
                    .into(imageView)
        }
        fun setToImageView(root: View, imageUrl: String, imageView: ImageView){
            if(!imageUrl.isNullOrEmpty()){
                Glide.with(root)
                    .load(imageUrl)
                    .into(imageView)
            }
        }
    }
}
//https://v3img.voot.com/resizeHigh,w_75,h_75/v3Storage/menu/myvoot_v2_active.png
//https://dev.w3.org/SVG/tools/svgweb/samples/svg-files/410.svg