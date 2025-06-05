const express = require('express');
const mongoose = require('mongoose');
const router = express.Router();
const app = express();
const port = 3000;

const bodyParser = require('body-parser');
app.use(bodyParser.json());

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
router.get('/list_product', async (req, res)=>{
    await mongoose.connect(uri);
    let product = await productModel.find();
    res.send(product);
});

// thêm product 'http://localhost:3000/api/add_product'
router.post('/add_product', async (req, res)=>{
    
    let data = req.body;
    let kq = await productModel.create(data);

    if(kq){
        console.log('Thêm sản phẩm thành công');
        let pro = await productModel.find();
        res.send(pro);
    } else{
        console.log('Thêm sản phẩm không thành công');
    }

})

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

// ds comment 'http://localhost:3000/api/list_comment'
router.get('/api/cart/:userId', async (req, res) => {
    try {
        const cartItems = await cartModel.find({ id_user: req.params.userId });
        res.json(cartItems);
    } catch (error) {
        console.error('Lỗi khi lấy giỏ hàng:', error);
        res.status(500).json({ error: 'Lỗi khi lấy giỏ hàng' });
    }
});

// thêm comment 'http://localhost:3000/api/add_comment'
router.post('/add_comment', async (req, res)=>{
    
    let data = req.body;
    let kq = await commentModel.create(data);

    if(kq){
        console.log('Thêm comment thành công');
        let comment = await commentModel.find();
        res.send(comment);
    } else{
        console.log('Thêm comment không thành công');
    }

})

// sửa comment 'http://localhost:3000/api/up_comment/ id'
router.put('/up_comment/:id', async (req, res)=>{
    try{
        const id = req.params.id;
        const data = req.body;
        
        const kq = await commentModel.findByIdAndUpdate(id, data, { new: true });

        if(kq){
            console.log('Sửa thành công');
            let usr = await commentModel.find();
            res.send(usr);
        } else{
            res.send('Không tìm thấy comment để sửa');
        }
    } catch (error){
        res.send('Lỗi khi sửa')
    }
})

router.post('/add_category',async(req,res)=>{

    let data = req.body;
    let kq = await categoryModel.create(data);

    if(kq){
        console.log('Thêm thể loại thành công!');
        let cate = await categoryModel.find();
        res.send(cate);
        
    }else{
        console.log('Thêm thể loại không thành công!');
        
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

//Comment
// ds comment 'http://localhost:3000/api/list_comment'
router.get('/api/list_comment/:userId', async (req, res) => {
    try {
        const cartItems = await cartModel.find({ id_user: req.params.userId });
        res.json(cartItems);
    } catch (error) {
        console.error('Lỗi', error);
        res.status(500).json({ error: 'Lỗi' });
    }
});

// thêm comment 'http://localhost:3000/api/add_comment'
router.post('/add_comment', async (req, res)=>{
    
    let data = req.body;
    let kq = await commentModel.create(data);

    if(kq){
        console.log('Thêm comment thành công');
        let comment = await commentModel.find();
        res.send(comment);
    } else{
        console.log('Thêm comment không thành công');
    }

})

// sửa comment 'http://localhost:3000/api/up_comment/ id'
router.put('/up_comment/:id', async (req, res)=>{
    try{
        const id = req.params.id;
        const data = req.body;
        
        const kq = await commentModel.findByIdAndUpdate(id, data, { new: true });

        if(kq){
            console.log('Sửa thành công');
            let usr = await commentModel.find();
            res.send(usr);
        } else{
            res.send('Không tìm thấy comment để sửa');
        }
    } catch (error){
        res.send('Lỗi khi sửa')
    }
})

app.use(express.json()); // bắt buộc để đọc req.body