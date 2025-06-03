const express = require('express');
const mongoose = require('mongoose');
const router = express.Router();
const app = express();
const cors = require('cors');
const port = 3000;

const bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));


const productModel = require('./Database/productModel');
const COMOMJS = require('./Database/COMOM');
const userModel = require('./Database/userModel');
const cartModel = require('./Database/cartModel');
const commentModel = require('./Database/commentModel');
const categoryModel = require('./Database/categoryModel');
const orderModel = require('./Database/order');

const uri = COMOMJS.uri;

// Kết nối MongoDB
mongoose.connect(uri, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
    serverSelectionTimeoutMS: 5000 // để không bị treo nếu không kết nối được
})
.then(() => console.log('Connected to MongoDB'))
.catch((err) => console.error('MongoDB connection error:', err));

// Khởi động server
app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});

const multer = require('multer');
const path = require('path');

const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/'); // đảm bảo thư mục này tồn tại
    },
    filename: (req, file, cb) => {
        cb(null, Date.now() + '-' + file.originalname);
    }
});

const upload = multer({ storage });
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));


app.get('/ds_product', async (req, res) => {
    try {
        const products = await productModel.find();
        console.log(products);
        res.json(products); // trả kết quả về client
    } catch (err) {
        console.error('Error fetching products:', err);
        res.status(500).json({ error: 'Lỗi khi lấy danh sách sản phẩm' });
    }
});

app.use('/api', router);

// lấy ds product 'http://localhost:3000/api/list_product'
router.get('/list_product', async (req, res) => {
    try {
        const products = await productModel.find().populate('id_category');
        res.json(products);
    } catch (error) {
        console.error('Lỗi khi lấy danh sách sản phẩm:', error);
        res.status(500).send('Lỗi server khi lấy sản phẩm');
    }
});

// thêm product 'http://localhost:3000/api/add_product'
router.post('/add_product', upload.fields([
    {name: 'avt_imgpro', maxCount: 1},
    {name: 'list_imgpro', maxCount: 10}
]), async (req, res)=>{
    
    try{
        const files = req.files;
        const body = req.body;
        
        const newPro = await productModel.create({
            nameproduct: body.name_pro,
            id_category: body.category_pro,
            price: body.price_pro,
            quantity: body.slg_pro,
            description: '',
            avt_imgproduct: files.avt_imgpro?.[0]?.filename || '',
            list_imgproduct: files.list_imgpro?.map(f => f.filename) || [],
            size: body.size_pro,
            color: body.color_pro,
            stock: 0,
            sold: 0
        });
        await newPro.save();
        console.log('Thêm sản phẩm thành công');
        const allProducts = await productModel.find();
        res.json(allProducts);
    } catch (error) {
        console.error('Thêm sản phẩm thất bại:', error);
        res.status(500).send('Lỗi server');
    }

});

// sửa product 'http://localhost:3000/api/up_product/ id'
router.put('/up_product/:id', async (req, res)=>{
    try{
        const id = req.params.id;
        const data = req.body;
        
        const kq = await productModel.findByIdAndUpdate(id, data, { new: true });

        if(kq){
            console.log('Sửa thành công');
            let pro = await productModel.find();
            res.send(pro);
        } else{
            res.send('Không tìm thấy sản phẩm để sửa');
        }
    } catch (error){
        res.send('Lỗi khi sửa')
    }
})

// xóa sản phẩm 'http://localhost:3000/api/del_product/ id'
router.delete('/del_product/:id', async (req, res)=>{
    try{
        let id = req.params.id;
        const kq = await productModel.deleteOne({_id: id});
        if(kq){
            console.log('Xóa sản phẩm thành công');
            let pro = await productModel.find();
            res.send(pro);
        } else{
            res.send('Xóa sản phẩm không thành công');
        }
    } catch(error){
        console.error('Lỗi khi xóa:', error);
        res.status(500).json({ error: 'Lỗi server khi xóa sản phẩm' });
    }
})

//User
// lấy ds user 'http://localhost:3000/api/list_user'
router.get('/list_user', async (req, res)=>{
    await mongoose.connect(uri);
    let user = await userModel.find();
    res.send(user);
});

// thêm user 'http://localhost:3000/api/add_user'
router.post('/add_user', async (req, res)=>{
    
    let data = req.body;
    let kq = await userModel.create(data);

    if(kq){
        console.log('Thêm người dùng thành công');
        let usr = await userModel.find();
        res.send(usr);
    } else{
        console.log('Thêm người dùng không thành công');
    }

})

// sửa user 'http://localhost:3000/api/up_user/ id'
router.put('/up_user/:id', async (req, res)=>{
    try{
        const id = req.params.id;
        const data = req.body;
        
        const kq = await userModel.findByIdAndUpdate(id, data, { new: true });

        if(kq){
            console.log('Sửa thành công');
            let usr = await userModel.find();
            res.send(usr);
        } else{
            res.send('Không tìm thấy người dùng để sửa');
        }
    } catch (error){
        res.send('Lỗi khi sửa')
    }
})

// xóa user 'http://localhost:3000/api/del_user/ id'
router.delete('/del_user/:id', async (req, res)=>{
    try{
        let id = req.params.id;
        const kq = await userModel.deleteOne({_id: id});
        if(kq){
            console.log('Xóa thành công');
            let usr = await userModel.find();
            res.send(usr);
        } else{
            res.send('Xóa không thành công');
        }
    } catch(error){
        console.error('Lỗi khi xóa:', error);
        res.status(500).json({ error: 'Lỗi server' });
    }
})

// Lấy giỏ hàng http://localhost:3000/api/:useId
router.get('/api/cart/:userId', async (req, res) => {
    try {
        const cartItems = await cartModel.find({ id_user: req.params.userId });
        res.json(cartItems);
    } catch (error) {
        console.error('Lỗi khi lấy giỏ hàng:', error);
        res.status(500).json({ error: 'Lỗi khi lấy giỏ hàng' });
    }
});

// thêm giỏ hàng http://localhost:3000/api/cart
router.post('/cart', async (req, res) => {
    const { id_user, id_product,nameproduct, image_product, quantity, price } = req.body;
    try {
        const total = quantity * price;

        let existing = await cartModel.findOne({ id_user, id_product});

        if (existing) {
            existing.quantity += quantity;
            existing.total = existing.quantity * price;
            await existing.save();
            return res.json(existing);
        }

        const item = new cartModel({ id_user, id_product, nameproduct, image_product, quantity, price, total });
        await item.save();
        res.status(201).json(item);
    } catch (error) {
        console.error('Lỗi thêm vào giỏ hàng:', error);
        res.status(500).json({ error: 'Lỗi thêm vào giỏ hàng' });
    }
});

// cập nhập só lượng trong giỏ hàng http://localhost:3000/api/cart/:idCart
router.put('/cart/:idCart', async (req, res) => {
    const { quantity } = req.body;
    try {
        const item = await cartModel.findById(req.params.idCart);
        if (!item) return res.status(404).json({ error: 'Không tìm thấy sản phẩm trong giỏ' });

        item.quantity = quantity;
        item.total = quantity * item.price;
        await item.save();
        res.json(item);
    } catch (error) {
        console.error('Lỗi cập nhật giỏ hàng:', error);
        res.status(500).json({ error: 'Lỗi cập nhật giỏ hàng' });
    }
});

// xoá giỏ hàng http://localhost:3000/api/cart/user/:userId
router.delete('/cart/:idCart', async (req, res) => {
    try {
        const result = await cartModel.deleteOne({ _id: req.params.idCart });
        if (result.deletedCount === 0) return res.status(404).json({ error: 'Không tìm thấy sản phẩm để xóa' });

        res.json({ message: 'Xóa sản phẩm thành công' });
    } catch (error) {
        console.error('Lỗi xóa sản phẩm:', error);
        res.status(500).json({ error: 'Lỗi xóa sản phẩm' });
    }
});

// xoá toàn bộ giỏ hàng http://localhost:3000/api/cart/user/:userId
router.delete('/cart/user/:userId', async (req, res) => {
    try {
        await cartModel.deleteMany({ id_user: req.params.userId });
        res.json({ message: 'Đã xóa toàn bộ giỏ hàng của người dùng' });
    } catch (error) {
        console.error('Lỗi xóa toàn bộ giỏ hàng:', error);
        res.status(500).json({ error: 'Lỗi xóa toàn bộ giỏ hàng' });
    }
});
// category
//lấy ds category 
router.get('/list_category',async(req,res)=>{
    await mongoose.connect(uri);
    let category = await categoryModel.find();
    res.send(category);
});


router.post('/add_category', upload.single('imgTL'),async(req,res)=>{


    try{
        const titleTL = req.body.titleTL;
        const imgTL = req.file ? req.file.filename : null;
        console.log("🟢 File:", req.file);
        console.log("🟢 File name:", imgTL);
        const newTL = new categoryModel({
            title: titleTL,
            cateImg: imgTL
        });
        const kq = await newTL.save();
        console.log('Thêm thể loại thành công');
        let category = await categoryModel.find();
        res.send(category);
    } catch (error) {
        console.error('Thêm thể loại thất bại:', error);
        res.status(500).send('Lỗi server');
    }
})
// sua category
router.put('/edit_cate/:id',async (req,res)=>{
    try{
        const id = req.params.id;
        const data = req.body;

        const kq = await categoryModel.findByIdAndUpdate(id,data, {new: true});

        if(kq){
            console.log('Sửa thành công!');
            let cate = await categoryModel.find();
            res.send('Không tìm thấy thể loại để sửa!');
            
        }
    }catch(err){
        res.send('Lỗi khi sửa')
    }
})
//xoa the loai
router.delete('/del_category/:id',async (req,res)=>{
    try{
        let id = req.params.id;
        const kq = await categoryModel.deleteOne({_id: id});
        if(kq){
            console.log('Xóa thể loại thành công!');
            let cate = await categoryModel.find();
            res.send(cate);
            
        }else{
            res.send('Xóa thể loại không thành công!');
        }
    }catch(err){
        console.error('Lỗi khi xóa: ', err);
        res.status(500).json({error: 'Lỗi server khi xóa thể loại '});
        
    }
})

// lấy ds don hang 'http://localhost:3000/api/list_order'
router.get('/list_order', async (req, res)=>{
    await mongoose.connect(uri);
    let order = await orderModel.find();
    res.send(order);
});

// thêm order 'http://localhost:3000/api/order'
router.post('/add_order', async (req, res)=>{
    
    let data = req.body;
    let kq = await orderModel.create(data);

    if(kq){
        console.log('Thêm don hang thành công');
        let ord = await orderModel.find();
        res.send(ord);
    } else{
        console.log('Thêm don hang không thành công');
    }

})

// huy don hang 'http://localhost:3000/api/order/ id'
router.delete('/del_order/:id', async (req, res)=>{
    try{
        let id = req.params.id;
        const kq = await orderModel.deleteOne({_id: id});
        if(kq){
            console.log('Huy don hang thành công');
            let ord = await orderModel.find();
            res.send(ord);
        } else{
            res.send('Huy don hang không thành công');
        }
    } catch(error){
        console.error('Lỗi khi xóa:', error);
        res.status(500).json({ error: 'Lỗi server khi xóa sản phẩm' });
    }
})

app.use(express.json()); // bắt buộc để đọc req.body