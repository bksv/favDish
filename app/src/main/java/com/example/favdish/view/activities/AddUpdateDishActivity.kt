package com.example.favdish.view.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.provider.SyncStateContract
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.ActivityAddUpdateDishBinding
import com.example.favdish.databinding.DialogCustomImageSelectionBinding
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.utils.Constants
import com.example.favdish.view.adapters.CustomListItemAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddUpdateDishActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddUpdateDishBinding

    private var mImagePath: String = ""

    private var _favDishDetails: FavDish? = null

    private lateinit var customListDialog: Dialog
    //creating an instance of ViewModel and setting connections to rep, and rep to Dao
    private val mFavDishViewModel: FavDishViewModel by viewModels{
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.EXTRA_DISH_DETAILS)){
            _favDishDetails = intent.getParcelableExtra(Constants.EXTRA_DISH_DETAILS)
        }

        setupActionBar()

        //if we passed a dish to be edited we'll populate the layout with this data to edit
        _favDishDetails?.let {
            if (it.id != 0) {
                mImagePath = it.image

                // Load the dish image in the ImageView.
                Glide.with(this)
                    .load(mImagePath)
                    .centerCrop()
                    .into(binding.ivDishImage)

                binding.etTitle.setText(it.title)
                binding.etType.setText(it.type)
                binding.etCategory.setText(it.category)
                binding.etIngredients.setText(it.ingredients)
                binding.etCookingTime.setText(it.cookingTime)
                binding.etDirectionToCook.setText(it.directionToCook)
                //changing text for btn
                binding.btnAddDish.text = resources.getString(R.string.lbl_update_dish)
            }
        }

        onClick()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding.toolbarAddDishActivity)//enabling the support of the toolbar
        //setting up toolbar text based on action required(editing or adding)
        if (_favDishDetails != null && _favDishDetails!!.id != 0){
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_edit_dish)
            }
        }
        else{
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_add_dish)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)//allowing the back btn on the toolbar
        binding.toolbarAddDishActivity.setNavigationOnClickListener {
            onBackPressed()//pressing the back btn will be the same event as on the phone
        }
    }

    /**
     * Setting up Listeners
     */
    private fun onClick(){
        binding.ivAddDishImage.setOnClickListener {
            customImageSelectionDialog()
        }
        binding.etType.setOnClickListener {
            customItemsListDialog(resources.getString(R.string.title_select_dish_type),
            Constants.dishTypes(), Constants.DISH_TYPE)
        }
        binding.etCategory.setOnClickListener {
            customItemsListDialog(resources.getString(R.string.title_select_dish_category),
                Constants.dishCategory(), Constants.DISH_CATEGORY)
        }
        binding.etCookingTime.setOnClickListener {
            customItemsListDialog(resources.getString(R.string.title_select_dish_cooking_time),
                Constants.dishCookTime(), Constants.DISH_COOKING_TIME)
        }
        binding.btnAddDish.setOnClickListener {
            //removing unnecessary spaces in our inputs
            val title = binding.etTitle.text.toString().trim { it <= ' ' }
            val type = binding.etType.text.toString().trim { it <= ' ' }
            val category = binding.etCategory.text.toString().trim { it <= ' ' }
            val ingredients = binding.etIngredients.text.toString().trim { it <= ' ' }
            val cookingTimeInMinutes = binding.etCookingTime.text.toString().trim { it <= ' ' }
            val cookingDirection = binding.etDirectionToCook.text.toString().trim { it <= ' ' }
            //check if any of the fields is empty
            when {

                TextUtils.isEmpty(mImagePath) -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.err_msg_select_dish_image),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(title) -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.err_msg_enter_dish_title),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(type) -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.err_msg_select_dish_type),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(category) -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.err_msg_select_dish_category),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(ingredients) -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.err_msg_enter_dish_ingredients),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(cookingTimeInMinutes) -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.err_msg_select_dish_cooking_time),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(cookingDirection) -> {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    var dishID = 0
                    var imageSource = Constants.DISH_IMAGE_SOURCE_LOCAL
                    var favoriteDish = false

                //if our editable object exists and valid
                    _favDishDetails?.let {
                        if (it.id != 0){
                            dishID = it.id
                            imageSource = it.imageSource
                            favoriteDish = it.favoriteDish
                        }
                    }


                //if all entries are valid we proceed and write data to the db
                    val favDishDetails = FavDish(
                        mImagePath,
                        imageSource,
                        title,
                        type,
                        category,
                        ingredients,
                        cookingTimeInMinutes,
                        cookingDirection,
                        favoriteDish,
                        dishID
                    )

                    if (dishID == 0){
                        mFavDishViewModel.insertDish(favDishDetails)
                        Toast.makeText(
                            this,
                            "You successfully added your favorite dish details",
                            Toast.LENGTH_SHORT).show()
                    }else{
                        mFavDishViewModel.update(favDishDetails)
                    }
                    finish()//finishing our activity
                }
            }
        }
    }

    private fun customImageSelectionDialog(){
        val dialog = Dialog(this)//creating a new dialog object
        //creating a new binding object as we'll use another layout for our dialog
        val dialogbinding: DialogCustomImageSelectionBinding = DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogbinding.root)


        //setting listeners for options
        dialogbinding.tvCamera.setOnClickListener {
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {//code only runs if report isn't empty
                        if(report.areAllPermissionsGranted()){
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)//opening the camera
                            startActivityForResult(intent, CAMERA)//we'll get our image with CAMERA code
                        }
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
            dialog.dismiss()//closing the dialog after option is chosen
        }

        dialogbinding.tvGallery.setOnClickListener {
            Dexter.withContext(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object: PermissionListener{
                    override fun onPermissionGranted(report: PermissionGrantedResponse?) {
                        val galleryIntent = Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(galleryIntent, GALLERY)
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Toast.makeText(this@AddUpdateDishActivity,
                            "You have denied the storage permission to select image", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        premission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread().check()
            dialog.dismiss()
        }
        dialog.show()
    }

    //if the user has not granted us access
    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("It looks like you have turned off permissions" +
                "required for this feature. It can be enabled under Applications settings")
            .setPositiveButton("GO TO SETTINGS")
            {_,_ -> //the variables we are getting when positive button is pressed and we don't need them, then we use lambda
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()//?
                }
            }
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    //if the result is fine and we get the image we expect, and request code is correct
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == CAMERA){
                data?.extras?.let {
                    val thumbnail: Bitmap = data.extras!!.get("data") as Bitmap//getting the data as a bitmap
                    //using glide to load and set the image to the imageView
                    Glide.with(this)
                        .load(thumbnail)
                        .centerCrop()
                        .into(binding.ivDishImage)
                    //saving image after we get it from camera and set it to imageView
                    mImagePath = saveImageToInternalStorage(thumbnail)
                    Log.i("ImagePathCamera", mImagePath)

                    binding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_vector_edit))
                }
            }
            else if (requestCode == GALLERY){
                data?.let {

                    val selectedPhotoUri = data.data//once we selected the image we'll get that as a data

                    Glide.with(this)
                        .load(selectedPhotoUri)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener((object : RequestListener<Drawable>{
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("TAG", "Error loading image", e)
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                resource?.let {
                                    val bitmap: Bitmap = resource.toBitmap()
                                    mImagePath = saveImageToInternalStorage(bitmap)
                                    Log.i("ImagePathGallery", mImagePath)
                                }
                                return false
                            }

                        }))
                        .into(binding.ivDishImage)

                    binding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_vector_edit))
                }
            }
        }else if (resultCode == Activity.RESULT_CANCELED){//if the user cancelled the operation
            Log.e("cancelled", "User cancelled image selection")
        }

    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String{
        val wrapper = ContextWrapper(applicationContext)//so the OS would know which app the image belongs to

        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)//in this mode image accessible only through this app
        file = File(file, "${UUID.randomUUID()}.jpg")//creating a file with directory and name

        try {
            val stream: OutputStream = FileOutputStream(file)//we need this so our bitmap can be compressed
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)//compress format and end quality of the image
            stream.flush()//?
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return file.absolutePath//returning the path to image(directory and name)
    }

    private fun customItemsListDialog(title: String, itemsList: List<String>, selection: String){
        customListDialog = Dialog(this)
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        customListDialog.setContentView(binding.root)

        binding.tvCustomDialogList.text = title

        binding.rvList.layoutManager = LinearLayoutManager(this)
        //setting up the adapter we create to our recyclerView
        val adapter = CustomListItemAdapter(this, null, itemsList, selection)
        binding.rvList.adapter = adapter

        customListDialog.show()
    }

    fun selectedListItem(item: String, selection: String){
        when(selection){
            Constants.DISH_TYPE ->{
                customListDialog.dismiss()
                binding.etType.setText(item)
            }
            Constants.DISH_CATEGORY ->{
                customListDialog.dismiss()
                binding.etCategory.setText(item)
            }
            Constants.DISH_COOKING_TIME ->{
                customListDialog.dismiss()
                binding.etCookingTime.setText(item)
            }
        }
    }

    companion object{
        private const val CAMERA = 1
        private const val GALLERY = 2

        private const val IMAGE_DIRECTORY = "FavDishImages"
    }

}